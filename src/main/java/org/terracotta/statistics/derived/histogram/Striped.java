/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Derived from code written by Doug Lea with assistance from members
 * of JCP JSR-166 Expert Group and released to the public domain,
 * as explained at http://creativecommons.org/publicdomain/zero/1.0/
 */

package org.terracotta.statistics.derived.histogram;

import java.util.Spliterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class Striped<T> {

  private static final int NCPU = Runtime.getRuntime().availableProcessors();

  private static final ThreadLocal<Integer> threadHash = ThreadLocal.withInitial(() -> 0);

  static final class Cell<T> {

    @SuppressWarnings("rawtypes")
    static final AtomicIntegerFieldUpdater<Cell> GUARD_UPDATER = AtomicIntegerFieldUpdater.newUpdater(Cell.class, "guard");

    final T entity;
    volatile int guard;

    Cell(T value) {
      entity = requireNonNull(value);
    }

    final boolean process(Consumer<T> process) {
      if (GUARD_UPDATER.compareAndSet(this, 0, 1)) {
        try {
          process.accept(entity);
          return true;
        } finally {
          GUARD_UPDATER.set(this, 0);
        }
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      while (!GUARD_UPDATER.compareAndSet(this, 0, 1));
      try {
        return entity.toString();
      } finally {
        GUARD_UPDATER.set(this, 0);
      }
    }
  }

  private final AtomicInteger stripeGuard = new AtomicInteger();

  private final Supplier<T> constructor;

  private final Cell<T> base;

  private volatile Cell<T>[] cells;

  static final int advanceProbe(int probe) {
    probe ^= probe << 13;
    probe ^= probe >>> 17;
    probe ^= probe << 5;
    threadHash.set(probe);
    return probe;
  }

  public Striped(Supplier<T> constructor) {
    this.constructor = constructor;
    this.base = new Cell<>(constructor.get());
  }

  protected final Stream<T> stream() {
    Cell<T>[] cs = cells;
    if (cs == null) {
      return of(base).map(cell -> cell.entity);
    } else {
      return concat(of(base), StreamSupport.stream(new CellSpliterator<T>(cells), false)).map(cell -> cell.entity);
    }
  }

  protected final void process(Consumer<T> process) {
    Cell<T>[] cs = cells;
    if (cs != null || !base.process(process)) {
      //either already striped - or going striped due to contention
      Cell<T> cell = null;
      boolean contended = false;
      int hash = threadHash.get();
      if (cs == null || (cell = cs[hash & (cs.length - 1)]) == null || (contended = !cell.process(process))) {
        processWithContention(hash, process, contended);
      }
    }
  }

  private void processWithContention(int hash, Consumer<T> process, boolean contended) {
    if (hash == 0) {
      threadHash.set((hash = ThreadLocalRandom.current().nextInt()));
      contended = false;
    }

    boolean collide = false;                // True if last slot nonempty
    for (;;) {
      Cell<T>[] cs;
      Cell<T> cell;
      if ((cs = cells) != null) {
        int n = cs.length;
        if ((cell = cs[(n - 1) & hash]) == null) {
          if (stripeGuard.get() == 0) {
            Cell<T> r = new Cell<>(constructor.get());   // Optimistically create
            r.process(process);
            if (stripeGuard.compareAndSet(0, 1)) {
              try {               // Recheck under lock
                Cell<T>[] rereadCells;
                int m, j;
                if ((rereadCells = cells) != null && (m = rereadCells.length) > 0
                    && rereadCells[j = (rereadCells.length - 1) & hash] == null) {
                  rereadCells[j] = r;
                  return;
                }
              } finally {
                stripeGuard.set(0);
              }
              continue;           // Slot is now non-empty
            }
          }
          collide = false;
        } else if (contended) {      // CAS already known to fail
          contended = false;      // Continue after rehash
        } else if (cell.process(process)) {
          return;
        } else if (n >= NCPU || cells != cs) {
          collide = false;            // At max size or stale
        } else if (!collide) {
          collide = true;
        } else if (stripeGuard.compareAndSet(0, 1)) {
          try {
            if (cells == cs) {      // Expand table unless stale
              @SuppressWarnings("unchecked")
              Cell<T>[] rs = (Cell<T>[]) new Cell<?>[cs.length << 1];
              for (int i = 0; i < cs.length; ++i)
                rs[i] = cs[i];
              cells = rs;
            }
          } finally {
            stripeGuard.set(0);
          }
          collide = false;
          continue;                   // Retry with expanded table
        }
        hash = advanceProbe(hash);
      } else if (stripeGuard.get() == 0 && cells == null && stripeGuard.compareAndSet(0, 1)) {
        try {                           // Initialize table
          if (cells == null) {
            @SuppressWarnings("unchecked")
            Cell<T>[] newCells = (Cell<T>[]) new Cell<?>[2];
            cell = new Cell<>(constructor.get());
            cell.process(process);
            newCells[hash & 1] = cell;
            cells = newCells;
            return;
          }
        } finally {
          stripeGuard.set(0);
        }
      } else if (base.process(process)) {
        return;
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getClass().getSimpleName()).append(":").append(System.lineSeparator())
        .append("\tBase: ").append(base).append(System.lineSeparator());
    for (Cell<T> cell : cells) {
      if (cell != null) {
        builder = builder.append("\tCell: ").append(cell).append(System.lineSeparator());
      }
    }
    return builder.toString();
  }

  static final class CellSpliterator<T> implements Spliterator<Cell<T>> {

    private final Cell<T>[] array;
    private int index;
    private final int limit;

    public CellSpliterator(Cell<T>[] array) {
      this(array, 0, array.length);
    }

    private CellSpliterator(Cell<T>[] array, int origin, int fence) {
      this.array = array;
      this.index = origin;
      this.limit = fence;
    }

    @Override
    public Spliterator<Cell<T>> trySplit() {
      int midpoint = (index + limit) >>> 1;

      if (index >= midpoint) {
        return null;
      } else {
        int splitIndex = index;
        index = midpoint;
        return new CellSpliterator<>(array, splitIndex, midpoint);
      }
    }

    @Override
    public void forEachRemaining(Consumer<? super Cell<T>> action) {
      requireNonNull(action);

      for (int i = index; i < limit; i++) {
        Cell<T> cell = array[i];
        if (cell != null) {
          visitCell(cell, action);
        }
      }
      index = limit;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Cell<T>> action) {
      requireNonNull(action);

      while (index >= 0 && index < limit) {
        Cell<T> cell = array[index++];
        if (cell != null) {
          visitCell(cell, action);
          return true;
        }
      }
      return false;
    }

    private static <T> void visitCell(Cell<T> cell, Consumer<? super Cell<T>> action) {
      while (!Cell.GUARD_UPDATER.compareAndSet(cell, 0, 1));
      try {
        action.accept(cell);
      } finally {
        Cell.GUARD_UPDATER.set(cell, 0);
      }
    }

    @Override
    public long estimateSize() {
      return limit - index;
    }

    @Override
    public int characteristics() {
      return Spliterator.SIZED | Spliterator.SUBSIZED;
    }
  }
}

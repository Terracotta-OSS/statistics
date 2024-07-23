/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.statistics.derived.latency;

import org.terracotta.statistics.observer.ChainedEventObserver;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.terracotta.statistics.Time.time;

/**
 * @author cdennis
 * @author Mathieu Carbou
 */
public class LatencySimpleMovingAverage implements ChainedEventObserver, LatencyStatistic {

  private static final int PARTITION_COUNT = 10;

  private final Queue<LatencyPeriodAccumulator> archive = new ConcurrentLinkedQueue<>();
  private final AtomicReference<LatencyPeriodAccumulator> activePartition;

  private final long windowSize;
  private final long partitionSize;

  public LatencySimpleMovingAverage(long time, TimeUnit unit) {
    this(time, unit, PARTITION_COUNT);
  }

  public LatencySimpleMovingAverage(long time, TimeUnit unit, int partitionCount) {
    this.windowSize = unit.toNanos(time);
    this.partitionSize = windowSize / partitionCount;
    this.activePartition = new AtomicReference<>(new LatencyPeriodAccumulator(Long.MIN_VALUE, partitionSize));
  }

  @Override
  public final double average() {
    long startTime = time() - windowSize;

    LatencyPeriodAccumulator current = activePartition.get();
    if (current.isBefore(startTime)) {
      return Double.NaN;
    } else {
      long total = current.accumulator().total();
      long count = current.accumulator().count();

      for (Iterator<LatencyPeriodAccumulator> it = archive.iterator(); it.hasNext(); ) {
        LatencyPeriodAccumulator partition = it.next();
        if (partition == current) {
          break;
        } else if (partition.isBefore(startTime)) {
          it.remove();
        } else {
          total += partition.accumulator().total();
          count += partition.accumulator().count();
        }
      }

      return ((double) total) / count;
    }
  }

  @Override
  public final Long maximum() {
    long startTime = time() - windowSize;

    LatencyPeriodAccumulator current = activePartition.get();
    if (current.isBefore(startTime)) {
      return null;
    } else {
      long maximum = current.maximum();
      for (Iterator<LatencyPeriodAccumulator> it = archive.iterator(); it.hasNext(); ) {
        LatencyPeriodAccumulator partition = it.next();
        if (partition == current) {
          break;
        } else if (partition.isBefore(startTime)) {
          it.remove();
        } else {
          maximum = Math.max(maximum, partition.maximum());
        }
      }

      return maximum;
    }
  }

  @Override
  public final Long minimum() {
    long startTime = time() - windowSize;

    LatencyPeriodAccumulator current = activePartition.get();
    if (current.isBefore(startTime)) {
      return null;
    } else {
      long minimum = current.minimum();
      for (Iterator<LatencyPeriodAccumulator> it = archive.iterator(); it.hasNext(); ) {
        LatencyPeriodAccumulator partition = it.next();
        if (partition == current) {
          break;
        } else if (partition.isBefore(startTime)) {
          it.remove();
        } else {
          minimum = Math.min(minimum, partition.minimum());
        }
      }

      return minimum;
    }
  }

  @Override
  public void event(long timeNs, long latencyNs) {
    while (true) {
      LatencyPeriodAccumulator partition = activePartition.get();
      if (partition.tryAccumulate(timeNs, latencyNs)) {
        return;
      } else {
        if (activePartition.compareAndSet(partition, new LatencyPeriodAccumulator(timeNs, partitionSize, latencyNs))) {
          archive(partition);
          return;
        }
      }
    }
  }

  private void archive(LatencyPeriodAccumulator partition) {
    archive.add(partition);
    long startTime = partition.end() - windowSize;
    for (LatencyPeriodAccumulator earliest = archive.peek(); earliest != null && earliest.isBefore(startTime); earliest = archive.peek()) {
      if (archive.remove(earliest)) {
        break;
      }
    }
  }

}

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
package org.terracotta.statistics.archive;

import org.terracotta.statistics.Sample;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author cdennis
 */
public class StatisticArchive<T extends Serializable> {

  private static final Comparator<Sample<?>> TIMESTAMPED_COMPARATOR = Comparator.comparingLong(Sample::getTimestamp);

  private final Consumer<? super Sample<T>> overspill;

  private volatile int size;
  private volatile CircularBuffer<Sample<T>> buffer;

  public StatisticArchive(int size) {
    this(size, sample -> {});
  }

  public StatisticArchive(int size, Consumer<? super Sample<T>> overspill) {
    this.size = size;
    this.overspill = overspill;
  }

  public synchronized void setCapacity(int samples) {
    if (samples != size) {
      size = samples;
      if (buffer != null) {
        CircularBuffer<Sample<T>> newBuffer = new CircularBuffer<>(size);
        for (Sample<T> sample : getArchive()) {
          overspill.accept(newBuffer.insert(sample));
        }
        buffer = newBuffer;
      }
    }
  }

  public synchronized void add(Sample<T> object) {
    if (buffer == null) {
      buffer = new CircularBuffer<>(size);
    }
    overspill.accept(buffer.insert(object));
  }

  public synchronized void clear() {
    buffer = null;
  }

  @SuppressWarnings("unchecked")
  public List<Sample<T>> getArchive() {
    CircularBuffer<Sample<T>> read = buffer;
    if (read == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(Arrays.<Sample<T>>asList(read.toArray(Sample[].class)));
    }
  }

  public List<Sample<T>> getArchive(long since) {
    CircularBuffer<Sample<T>> read = buffer;
    if (read == null) {
      return Collections.emptyList();
    } else {
      Sample<T> e = new Sample<>(since, null);
      @SuppressWarnings("unchecked")
      Sample<T>[] array = (Sample<T>[]) read.toArray(Sample[].class);
      int pos = Arrays.binarySearch(array, e, TIMESTAMPED_COMPARATOR);
      if (pos < 0) {
        pos = -pos - 1;
      }
      if (pos >= array.length) {
        return Collections.emptyList();
      } else {
        return Collections.unmodifiableList(Arrays.asList(Arrays.copyOfRange(array, pos, array.length)));
      }
    }
  }

}

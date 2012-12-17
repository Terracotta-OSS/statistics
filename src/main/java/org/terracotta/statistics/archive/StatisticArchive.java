/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author cdennis
 */
public class StatisticArchive<T> implements SampleSink<Timestamped<T>> {
  
  private final SampleSink<? super Timestamped<T>> overspill;

  private volatile int size;
  private volatile CircularBuffer<Timestamped<T>> buffer;
  
  public StatisticArchive(int size) {
    this(size, DevNull.DEV_NULL);
  }
  
  public StatisticArchive(int size, SampleSink<? super Timestamped<T>> overspill) {
    this.size = size;
    this.overspill = overspill;
  }
  
  public synchronized void setCapacity(int samples) {
    if (samples != size) {
      size = samples;
      if (buffer != null) {
        CircularBuffer<Timestamped<T>> newBuffer = new CircularBuffer<Timestamped<T>>(size);
        for (Timestamped<T> sample : getArchive()) {
          overspill.accept(newBuffer.insert(sample));
        }
        buffer = newBuffer;
      }
    }
  }
  
  @Override
  public synchronized void accept(Timestamped<T> object) {
    if (buffer == null) {
      buffer = new CircularBuffer<Timestamped<T>>(size);
    }
    overspill.accept(buffer.insert(object));
  }

  public synchronized void clear() {
    buffer = null;
  }
  
  public List<Timestamped<T>> getArchive() {
    CircularBuffer<Timestamped<T>> read = buffer;
    if (read == null) {
      return Collections.emptyList();
    } else {
      return Collections.unmodifiableList(Arrays.asList((Timestamped<T>[]) read.toArray(Timestamped[].class)));
    }
  }
}

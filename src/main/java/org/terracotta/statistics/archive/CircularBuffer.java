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

/**
 *
 * @author cdennis
 */
public class CircularBuffer<E> {
  
  private final E[] buffer;
  private int writeIndex;
  private int size;
  
  public CircularBuffer(int size) {
    this.buffer = (E[]) new Object[size];
  }
  
  public int capacity() {
    return buffer.length;
  }
  
  public synchronized E insert(E object) {
    E old = buffer[writeIndex];
    buffer[writeIndex] = object;
    writeIndex++;
    size = Math.max(writeIndex, size);
    writeIndex %= buffer.length;
    return old;
  }

  public synchronized <T> T[] toArray(Class<T[]> type) {
    if (size < buffer.length) {
      return Arrays.copyOfRange(buffer, 0, writeIndex, type);
    } else {
      T[] copy = Arrays.copyOfRange(buffer, writeIndex, writeIndex + buffer.length, type);
      System.arraycopy(buffer, 0, copy, buffer.length - writeIndex, writeIndex);
      return copy;
    }
  }
}

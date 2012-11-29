/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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

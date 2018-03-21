/*
 * Copyright 2015 Terracotta, Inc..
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
package org.terracotta.statistics.derived.histogram;

import java.util.Arrays;

import static java.lang.Long.MIN_VALUE;
import static java.lang.Long.highestOneBit;
import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Long.numberOfTrailingZeros;
import static java.lang.Math.max;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;

/**
 * An implementation of the Exponential Histogram sketch as outline by Datar et al.
 *
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.24.7941">
 *   Maintaining Stream Statistics over Sliding Windows</a>
 */
public class ExponentialHistogram {
  
  private static final long[] EMPTY_LONG_ARRAY = new long[0];

  private final float epsilon;
  private final int mergeThreshold;
  private final long window;
  
  private long[] boxes;
  private int[] insert;
  
  private long total;
  private long last;

  public ExponentialHistogram(float epsilon, long window) {
    this(epsilon, (int) (Math.ceil(Math.ceil(1.0 / epsilon) / 2) + 1), window, 0);
  }

  private ExponentialHistogram(float epsilon, int mergeThreshold, long window, int initialSize) {
    this.epsilon = epsilon;
    this.mergeThreshold = mergeThreshold;
    this.window = window;
    initializeArrays(initialSize);
  }
  
  public void merge(ExponentialHistogram b) {
    merge(b.boxes, b.last, b.total);
  }

  private void merge(long[] bBoxes, long bLast, long bTotal) {
    long[] aBoxes = this.boxes;

    int logLast = (Long.SIZE - 1) - numberOfLeadingZeros(last | bLast);
    initializeArrays(logLast);
    this.total += bTotal;
    
    long[] overflow = EMPTY_LONG_ARRAY;
    for (int logSize = 0; logSize <= logLast; logSize++) {
      overflow = insert(logSize, aBoxes, bBoxes, overflow);
    }
    int logSize = logLast + 1;
    while (overflow.length > 0) {
      ensureCapacity(logSize);
      overflow = insert(logSize, overflow);
      logSize++;
    }
    last = 1L << (logSize - 1);
  }

  public void insert(long time, long count) {
    if (count < 0) {
      throw new IllegalArgumentException("negative count");
    } else if (count == 0) {
      return;
    } else {
      if (time == MIN_VALUE) {
        //MIN_VALUE means a box is unused so we avoid it
        time++;
      }
      long[] bBoxes = makeBoxes(time, count);
      long bLast = 1L << (((bBoxes.length - mergeThreshold + 1) / mergeThreshold) - 1);
      merge(bBoxes, bLast, count);
    }
  }

  private long[] makeBoxes(long time, long count) {
    int[] canonical = tailedLCanonical(mergeThreshold - 1, count);


    long[] boxes = new long[min_l(canonical.length)];
    Arrays.fill(boxes, MIN_VALUE);

    for (int i = 0; i < canonical.length; i++) {
      int min = min_l(i);
      for (int a = min; a < min + canonical[i]; a++) {
        boxes[a] = time;
      }
    }
    return boxes;
  }

  private static int[] tailedLCanonical(int l, long count) {
    if (count < l) {
      return new int[]{(int) count};
    } else {
      int[] form = lCanonical(l, count - l);
      form[0] += l;
      return form;
    }
  }

  private static int[] lCanonical(int l, long count) {
    long num = count + l;
    long denom = l + 1;
    int j = numberOfTrailingZeros(highestOneBit(num / denom));

    long offset = (num - (denom << j));
    long prefixRep = offset & ((1L << j) - 1);

    int[] canonical = new int[j + 1];

    for (int i = 0; i < j; i++) {
      canonical[i] = l + (int) (((prefixRep >>> i) & 1));
    }

    canonical[j] = (int) ((offset >>> j) + 1);

    return canonical;
  }

  private long[] insert(int logSize, long[] values) {
    int min = min_l(logSize);
    int max = max_l(logSize);
    int width = max - min;
    int overflowSize = max(0, ((values.length - width) + 1) & -2);
    int savedSize = values.length - overflowSize;

    arraycopy(values, 0, boxes, max - savedSize, savedSize);
    insert[logSize] = max - ((savedSize % width) + 1);

    if (overflowSize == 0) {
      return EMPTY_LONG_ARRAY;
    } else {
      long[] overflow = new long[overflowSize >>> 1];
      for (int j = 0; j < overflow.length; j++) {
        overflow[j] = values[savedSize + (2 * j)];
      }
      return overflow;
    }
  }

  private long[] insert(int logSize, long[] a, long[] b, long[] carry) {
    int min = min_l(logSize);
    int max = max_l(logSize);
    long[] merged = reverseSort(merge(a, b, min, max, carry));
    int mergedEnd = usedLength(merged);
    int width = max - min;
    int overflowSize = max(0, ((mergedEnd - width) + 1) & -2);
    int savedSize = mergedEnd - overflowSize;
    
    arraycopy(merged, 0, boxes, max - savedSize, savedSize);
    insert[logSize] = max - ((savedSize % width) + 1);

    if (overflowSize == 0) {
      return EMPTY_LONG_ARRAY;
    } else {
      long[] overflow = new long[overflowSize >>> 1];
      for (int j = 0; j < overflow.length; j++) {
        overflow[j] = merged[savedSize + (2 * j)];
      }

      return overflow;
    }
  }

  private static long[] merge(long[] a, long[] b, int min, int max, long[] c) {
    if (max <= a.length) {
      if (max <= b.length) {
    	int width = max - min;
        long[] merged = copyOf(c, c.length + 2 * width);
        arraycopy(a, min, merged, c.length, width);
        arraycopy(b, min, merged, c.length + width, width);
        return merged;
      } else {
        return merge(a, min, max, c);
      }
    } else {
      return merge(b, min, max, c);
    }
  }

  private static long[] merge(long[] a, int min, int max, long[] c) {
    int width = max - min;
    long[] merged = copyOf(c, c.length + width);
    arraycopy(a, min, merged, c.length, width);
    return merged;
  }
  
  public void insert(long time) {
    if (time == MIN_VALUE) {
      time++;
    }
    insert_l(0, time);
  }

  private void insert_l(int initialLogSize, long time) {
    total += (1L << initialLogSize);
    for (int logSize = initialLogSize; ; logSize++) {
      ensureCapacity(logSize);
      
      int insertIndex = insert[logSize];
      long previous = boxes[insertIndex];
      boxes[insertIndex--] = time;
      if (insertIndex < min_l(logSize)) {
        insertIndex = max_l(logSize) - 1;
      }
      insert[logSize] = insertIndex;

      if (previous == MIN_VALUE) {
        //previous unoccupied
        long finalSize = 1L << logSize;
        if (finalSize > last) {
          last = finalSize;
        }
        return;
      } else if ((time - previous) < window) {
        //no space available - time to merge
        time = boxes[insertIndex];
        boxes[insertIndex] = MIN_VALUE;
      } else {
        //previous aged out - decrement size
        total -= 1L << logSize;
        return;
      }
    }
  }
  
  public void expire(long time) {
    for (int logSize = (Long.SIZE - 1) - numberOfLeadingZeros(last); logSize >= 0; logSize--) {
      boolean live = false;
      for (int i = min_l(logSize); i < max_l(logSize); i++) {
        long end = boxes[i];
        if (end != MIN_VALUE) {
          if ((time - end) >= window) {
            total -= 1L << logSize;
            boxes[i] = MIN_VALUE;
          } else {
            live = true;
          }
        }
      }
      if (live) {
        last = 1L << logSize;
        return;
      }
    }
    last = 0;
  }

  private int min_l(int logSize) {
    if (logSize == 0) {
      return 0;
    } else {
      return ((logSize + 1) * mergeThreshold) - 1;
    }
  }
  
  private int max_l(int logSize) {
    return ((logSize + 2) * mergeThreshold) - 1;
  }
  
  public boolean isEmpty() {
    return total == 0;
  }

  public long count() {
    return total - (last >>> 1);
  }
  
  public ExponentialHistogram split(float fraction) {
    long[] originalBoxes = boxes;
    int[] originalInsert = insert;

    int logLast = (Long.SIZE - 1) - numberOfLeadingZeros(last);
    ExponentialHistogram other = new ExponentialHistogram(epsilon, mergeThreshold, window, logLast - 1);
    initializeArrays(logLast - 1);
    this.total = 0;
    this.last = 0;
    
    { //extracted zero iteration
      int start = originalInsert[0] + 1;
      for (int i = start; i < max_l(0); i++) {
        long time = originalBoxes[i];
        if (time == MIN_VALUE) {
          break;
        } else if (other.total <= fraction * (other.total + this.total)) {
          other.insert_l(0, time);
        } else {
          insert_l(0, time);
        }
      }
      for (int i = 0; i < start; i++) {
        long time = originalBoxes[i];
        if (time == MIN_VALUE) {
          break;
        } else if (other.total <= fraction * (other.total + this.total)) {
          other.insert_l(0, time);
        } else {
          insert_l(0, time);
        }
      }
    }
    
    for (int logSize = 1; logSize < originalInsert.length; logSize++) {
      int start = originalInsert[logSize] + 1;
      for (int i = start; i < max_l(logSize); i++) {
        long time = originalBoxes[i];
        if (time == MIN_VALUE) {
          break;
        } else {
          for (int split = 0; split < 2; split++) {
            if (other.total <= fraction * (other.total + this.total)) {
              other.insert_l(logSize - 1, time);
            } else {
              this.insert_l(logSize - 1, time);
            }
          }
        }
      }
      for (int i = min_l(logSize); i < start; i++) {
        long time = originalBoxes[i];
        if (time == MIN_VALUE) {
          break;
        } else {
          for (int split = 0; split < 2; split++) {
            if (other.total <= fraction * (other.total + this.total)) {
              other.insert_l(logSize - 1, time);
            } else {
              this.insert_l(logSize - 1, time);
            }
          }
        }
      }
    }
    return other;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("count = ").append(count()).append(" : ");
    for (int logSize = 0; logSize < insert.length; logSize++) {
      for (int i = insert[logSize] + 1; i < max_l(logSize); i++) {
        long time = boxes[i];
        if (time != MIN_VALUE) {
          sb.append("[").append(1L << logSize).append("@").append(time).append("], ");
        }
      }
      for (int i = min_l(logSize); i < insert[logSize] + 1; i++) {
        long time = boxes[i];
        if (time != MIN_VALUE) {
          sb.append("[").append(1L << logSize).append("@").append(time).append("], ");
        }
      }
    }
    sb.delete(sb.length() - 2, sb.length());
    return sb.toString();
  }
  
  private static long[] reverseSort(long[] a) {
    for (int i = 0, j = i; i < a.length - 1; j = ++i) {
        long ai = a[i + 1];
        while (ai > a[j]) {
            a[j + 1] = a[j];
            if (j-- == 0) {
                break;
            }
        }
        a[j + 1] = ai;
    }
    return a;
  }

  private void ensureCapacity(int logSize) {
    int max = max_l(logSize);
    if (max > boxes.length) {
      long[] newBoxes = copyOf(boxes, max);
      int[] newInsert = copyOf(insert, logSize + 1);
      fill(newBoxes, boxes.length, newBoxes.length, MIN_VALUE);
      this.boxes = newBoxes;
      this.insert = newInsert;
      insert[logSize] = max - 1;
    }
  }

  private void initializeArrays(int logMax) {
    this.boxes = new long[max_l(logMax)];
    fill(boxes, Long.MIN_VALUE);
    this.insert = new int[logMax + 1];
    for (int i = 0; i < logMax + 1; i++) {
      this.insert[i] = max_l(i) - 1;
    }
  }

  private static int usedLength(long[] merged) {
    for (int i = merged.length - 1; i >= 0; i--) {
      if (merged[i] != MIN_VALUE) {
        return i + 1;
      }
    }
    return 0;
  }

  public float epsilon() {
    return epsilon;
  }
}

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
package org.terracotta.statistics.derived.histogram;

import java.util.Arrays;

import static java.lang.Long.MIN_VALUE;
import static java.lang.Long.highestOneBit;
import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Long.numberOfTrailingZeros;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;

/**
 * An implementation of the Exponential Histogram sketch as outlined by Datar et al.
 *
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.24.7941">
 *   Maintaining Stream Statistics over Sliding Windows</a>
 */
public class ExponentialHistogram {
  
  private static final long[] EMPTY_LONG_ARRAY = new long[0];

  private final double epsilon;
  private final int mergeThreshold;
  private final long window;
  
  private long[] boxes;
  private int[] insert;
  
  private long total;
  private long last;

  /**
   * Creates an exponential histogram maintaining a count over {@code window} to within @{epsilon} fractional accuracy.
   *
   * @param epsilon fractional accuracy
   * @param window sliding window size
   */
  public ExponentialHistogram(double epsilon, long window) {
    this(epsilon, (int) (Math.ceil(Math.ceil(1.0 / epsilon) / 2) + 1), window, 0);
  }

  private ExponentialHistogram(double epsilon, int mergeThreshold, long window, int initialSize) {
    this.epsilon = epsilon;
    this.mergeThreshold = mergeThreshold;
    this.window = window;
    initializeArrays(initialSize);
  }

  /**
   * Merge the supplied ExponentialHistogram in to this one.
   *
   * @param b histogram to merge
   * @throws IllegalArgumentException if the two merge-thresholds are not equals
   */
  public void merge(ExponentialHistogram b) {
    if (b.mergeThreshold != mergeThreshold) {
      throw new IllegalArgumentException();
    }
    merge(b.boxes, b.total);
  }

  private void merge(long[] bBoxes, long bTotal) {
    long[] aBoxes = this.boxes;
    long aTotal = this.total;

    int[] canonical = tailedLCanonical(mergeThreshold - 1, aTotal + bTotal);

    initializeArrays(canonical.length - 1);
    this.last = 1L << (canonical.length - 1);
    this.total = aTotal + bTotal;

    long[] overflow = EMPTY_LONG_ARRAY;
    for (int logSize = 0; logSize < canonical.length; logSize++) {
      int boxCount = canonical[logSize];

      int min = min_l(logSize);
      int max = max_l(logSize);
      long[] merged = reverseSort(merge(aBoxes, bBoxes, min, max, overflow));
      System.arraycopy(merged, 0, boxes, max - boxCount, boxCount);

      int width = max - min;
      if (boxCount == width) {
        insert[logSize] = (max_l(logSize) - 1);
      } else {
        insert[logSize] = (max_l(logSize) - 1) - boxCount;
      }

      int overflowSize = merged.length - boxCount;
      overflow = new long[overflowSize >>> 1];
      for (int j = 0; j < overflow.length; j++) {
        overflow[j] = merged[boxCount + (2 * j)];
      }
    }
  }

  /**
   * Bulk insert {@code count} events at {@code time}.
   *
   * @param time event time
   * @param count event count
   * @throws IllegalArgumentException if count is negative
  */
  public void insert(long time, long count) throws IllegalArgumentException {
    if (count < 0) {
      throw new IllegalArgumentException("negative count");
    } else if (count == 0) {
      return;
    } else {
      if (time == MIN_VALUE) {
        //MIN_VALUE means a box is unused so we avoid it
        time++;
      }
      merge(makeBoxes(time, count), count);
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
    if (count <= l) {
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

  private static long[] merge(long[] a, long[] b, int min, int max, long[] c) {
    int width = max - min;
    if (max <= a.length) {
      if (max <= b.length) {
        long[] merged = copyOf(c, c.length + 2 * width);
        arraycopy(a, min, merged, c.length, width);
        arraycopy(b, min, merged, c.length + width, width);
        return merged;
      } else {
        long[] merged = copyOf(c, c.length + width);
        arraycopy(a, min, merged, c.length, width);
        return merged;
      }
    } else if (max <= b.length) {
      long[] merged = copyOf(c, c.length + width);
      arraycopy(b, min, merged, c.length, width);
      return merged;
    } else {
      return c;
    }
  }

  /**
   * Insert a single event at {@code time}
   *
   * @param time event timestamp
   */
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

  /**
   * Expire old events.
   *
   * @param time current timestamp
   */
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

  /**
   * Returns the approximate current count.
   *
   * @return the approximate count
   */
  public long count() {
    return total - (last >>> 1);
  }

  /**
   * Split an exponential histogram off this one.
   * <p>
   *   The returned histogram will contains {code fraction} of the events in this one.
   * </p>
   * @param fraction splitting fraction
   * @return the new histogram
   */
  public ExponentialHistogram split(double fraction) {
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

  /**
   * Return the fractional accuracy of this exponential histogram
   *
   * @return fractional accuracy
   */
  public double epsilon() {
    return epsilon;
  }
}

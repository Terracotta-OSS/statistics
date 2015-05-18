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

import static java.lang.Long.MIN_VALUE;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.fill;

import java.util.Arrays;

public class ExponentialHistogram {
  
  /*
   * TODO: The original paper on this data structure doesn't (by my reading) say
   * anything about different merge thresholds for the 1-size boxes.  Need to
   * figure out if this is an error in the BASH/BSBH paper, or is an important
   * correction from the EH one.
   */
  
  private final int mergeThreshold;
  private final long window;
  
  private long[] boxes;
  private int[] insert;
  
  private long total;
  private long last;
  
  public ExponentialHistogram(float epsilon, long window) {
    this((int) (Math.ceil(Math.ceil(1 / epsilon) / 2) + 1), window);
  }
  
  private ExponentialHistogram(int mergeThreshold, long window) {
    this.mergeThreshold = mergeThreshold;
    this.window = window;
    this.boxes = new long[0];
    this.insert = new int[0];
  }
  
  public ExponentialHistogram(ExponentialHistogram a, ExponentialHistogram b) {
    this.mergeThreshold = a.mergeThreshold;
    this.window = a.window;
    this.boxes = new long[0];
    this.insert = new int[0];
    this.total = a.total + b.total;
    
    long[] overflow = new long[0];
    for (int i = 0; ; i++) {
      int min = min_l(i);
      int max = max_l(i);
      long[] aRange = range(a.boxes, min, max);
      long[] bRange = range(b.boxes, min, max);
      if (aRange.length == 0 && bRange.length == 0 && overflow.length == 0) {
        last = 1 << (i - 1);
        return;
      } else {
        overflow = insert(i, min, max, aRange, bRange, overflow);
      }
    }
  }
  
  private long[] insert(int logSize, int min, int max, long[] ... arrays) {
    if (max > boxes.length) {
      long[] newBoxes = copyOf(boxes, max);
      int[] newInsert = copyOf(insert, insert.length + 1);
      fill(newBoxes, boxes.length, newBoxes.length, MIN_VALUE);
      newInsert[insert.length] = max - 1;
      this.boxes = newBoxes;
      this.insert = newInsert;
    }

    long[] merged = merge(arrays);
    Arrays.sort(merged);
    for (int i = 0; i < merged.length; i++) {
      if (merged[i] != MIN_VALUE) {
        if (i != 0) {
          merged = copyOfRange(merged, i, merged.length);
        }
        break;
      }
    }
    int overflowSize = Integer.max(0, ((merged.length - (max - min)) + 1) & -2);
    long[] overflowed = copyOfRange(merged, 0, overflowSize);
    long[] saved = copyOfRange(merged, overflowSize, merged.length);

    for (int i = 0; i < saved.length; i++) {
      boxes[max - (i + 1)] = saved[i];
    }
    if (saved.length == (max - min)) {
      insert[logSize] = max - 1;
    } else {
      insert[logSize] = max - (saved.length + 1);
    }
    long[] overflow = new long[overflowed.length / 2];
    for (int i = 0; i < overflow.length; i++) {
      overflow[i] = overflowed[(i * 2) + 1];
    }
    
    return overflow;
  }
  
  private static long[] merge(long[] ... arrays) {
    int totalSize = 0;
    for (long[] array : arrays) {
      totalSize += array.length;
    }
    long[] merged = new long[totalSize];
    int mergedIndex = 0;
    for (long[] array : arrays) {
      arraycopy(array, 0, merged, mergedIndex, array.length);
      mergedIndex += array.length;
    }
    return merged;
  }

  private static long[] range(long[] array, int min, int max) {
    if (min < array.length) {
      return copyOfRange(array, min, Integer.min(max, array.length));
    } else {
      return new long[0];
    }
  }
  
  public void insert(long time) {
    insert_l(0, time);
    total++;
  }

  private void insert_l(int logSize, long time) {
    int max = max_l(logSize);
    if (max > boxes.length) {
      long[] newBoxes = copyOf(boxes, max);
      int[] newInsert = copyOf(insert, insert.length + 1);
      fill(newBoxes, boxes.length, newBoxes.length - 1, MIN_VALUE);
      newInsert[insert.length] = max - 2;
      this.boxes = newBoxes;
      this.insert = newInsert;
      boxes[max - 1] = time;
    } else {
      int insertIndex = insert[logSize];
      if (boxes[insertIndex] == Long.MIN_VALUE) {
        boxes[insertIndex] = time;
        last = Math.max(1 << logSize, last);
        if (--insertIndex < min_l(logSize)) {
          insertIndex = max - 1;
        }
        insert[logSize] = insertIndex;
      } else{
        //no space available - time to merge
        boxes[insertIndex] = time;
        if (--insertIndex < min_l(logSize)) {
          insertIndex = max - 1;
        }
        long boxUp = boxes[insertIndex];
        boxes[insertIndex] = MIN_VALUE;
        insert[logSize] = insertIndex;
        insert_l(logSize + 1, boxUp);
      }
    }
  }
  
  public void insertAndExpire(long time) {
    expire(time);
    insert(time);
  }
  
  public void expire(long time) {
    long threshold = time - window;
    for (int logSize = insert.length - 1; logSize >= 0; logSize--) {
      int insertIndex = insert[logSize];
      for (int i = insertIndex; i >= min_l(logSize); i--) {
        long end = boxes[i];
        if (end != MIN_VALUE) {
          if (end <= threshold) {
            total -= 1 << logSize;
            boxes[i] = MIN_VALUE;
          } else {
            last = 1 << logSize;
            return;
          }
        }
      }
      for (int i = max_l(logSize) - 1; i > insertIndex; i--) {
        long end = boxes[i];
        if (end != MIN_VALUE) {
          if (end <= threshold) {
            total -= 1 << logSize;
            boxes[i] = MIN_VALUE;
          } else {
            last = 1 << logSize;
            return;
          }
        }
      }
    }
    last = 0;
  }

  private int start_l(int logSize) {
    return insert[logSize] + 1;
  }
  
  private int min_l(int logSize) {
    if (logSize == 0) {
      return 0;
    } else {
      return (logSize * mergeThreshold) + 1;
    }
  }
  
  private int max_l(int logSize) {
    if (logSize == 0) {
      return mergeThreshold + 1; //1-size boxes have bigger merge thresholds?!
    } else {
      return ((logSize + 1) * mergeThreshold) + 1;
    }
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
    
    ExponentialHistogram other = new ExponentialHistogram(mergeThreshold, window);
    this.boxes = new long[0];
    this.insert = new int[0];
    this.total = 0;

    for (int logSize = 0; logSize < originalInsert.length; logSize++) {
      for (int i = originalInsert[logSize] + 1; i < max_l(logSize); i++) {
        long time = originalBoxes[i];
        if (logSize == 0) {
          if (other.total <= fraction * (other.total + this.total)) {
            other.insert_l(0, time);
            other.total += 1;
          } else {
            this.insert_l(0, time);
            this.total += 1;
          }
        } else {
          for (int split = 0; split < 2; split++) {
            if (other.total <= fraction * (other.total + this.total)) {
              other.insert_l(logSize - 1, time);
              other.total += 1 << (logSize - 1);
            } else {
              this.insert_l(logSize - 1, time);
              this.total += 1 << (logSize - 1);
            }
          }
        }
      }
      for (int i = min_l(logSize); i < originalInsert[logSize] + 1; i++) {
        long time = originalBoxes[i];
        if (logSize == 0) {
          if (other.total <= fraction * (other.total + this.total)) {
            other.insert_l(0, time);
            other.total += 1;
          } else {
            this.insert_l(0, time);
            this.total += 1;
          }
        } else {
          for (int split = 0; split < 2; split++) {
            if (other.total <= fraction * (other.total + this.total)) {
              other.insert_l(logSize - 1, time);
              other.total += 1 << (logSize - 1);
            } else {
              this.insert_l(logSize - 1, time);
              this.total += 1 << (logSize - 1);
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
      for (int i = start_l(logSize); i < max_l(logSize); i++) {
        long time = boxes[i];
        if (time != MIN_VALUE) {
          sb.append("[").append(1 << logSize).append("@").append(time).append("], ");
        }
      }
      for (int i = min_l(logSize); i < start_l(logSize); i++) {
        long time = boxes[i];
        if (time != MIN_VALUE) {
          sb.append("[").append(1 << logSize).append("@").append(time).append("], ");
        }
      }
    }
    sb.delete(sb.length() - 2, sb.length());
    return sb.toString();
  }
}

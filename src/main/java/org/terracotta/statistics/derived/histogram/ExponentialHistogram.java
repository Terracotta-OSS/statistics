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
  private int total;
  private int last;
  
  public ExponentialHistogram(float epsilon, long window) {
    this((int) (Math.ceil(Math.ceil(1 / epsilon) / 2) + 1), window);
  }
  
  private ExponentialHistogram(int mergeThreshold, long window) {
    this.mergeThreshold = mergeThreshold;
    this.window = window;
    this.boxes = new long[0];
  }
  
  public ExponentialHistogram(ExponentialHistogram a, ExponentialHistogram b) {
    this.mergeThreshold = a.mergeThreshold;
    this.window = a.window;
    this.boxes = new long[0];
    this.total = a.total + b.total;
    
    long[] overflow = new long[0];
    for (int i = 1; ; i <<= 1) {
      int min = min(i);
      int max = max(i);
      long[] aRange = range(a.boxes, min, max);
      long[] bRange = range(b.boxes, min, max);
      if (aRange.length == 0 && bRange.length == 0 && overflow.length == 0) {
        return;
      } else {
        last = i;
        overflow = insert(min, max, aRange, bRange, overflow);
      }
    }
  }
  
  private long[] insert(int min, int max, long[] ... arrays) {
    if (max > boxes.length) {
      long[] newBoxes = copyOf(boxes, max);
      fill(newBoxes, boxes.length, newBoxes.length, MIN_VALUE);
      this.boxes = newBoxes;
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
    insert(1, time);
    total++;
  }

  private void insert(int size, long time) {
    int max = max(size);
    if (max > boxes.length) {
      long[] newBoxes = copyOf(boxes, max);
      fill(newBoxes, boxes.length, newBoxes.length - 1, MIN_VALUE);
      this.boxes = newBoxes;
      boxes[max - 1] = time;
    } else {
      int min = min(size);
      if (boxes[min] == MIN_VALUE) {
        for (int i = min; i < max - 1 ; i++) {
          if (boxes[i + 1] != MIN_VALUE) {
            boxes[i] = time;
            last = Math.max(size, last);
            return;
          }
        }
        boxes[max - 1] = time;
        last = Math.max(size, last);
      } else {
        //no space available - time to merge
        long boxUp = boxes[max - 2];
        arraycopy(boxes, min, boxes, min + 2, max - min - 2);
        boxes[min + 1] = time;
        boxes[min] = MIN_VALUE;
        insert(size << 1, boxUp);
      }
    }
  }
  
  public void insertAndExpire(long time) {
    expire(time);
    insert(time);
  }
  
  public void expire(long time) {
    for (int i = boxes.length - 1; i >= 0; i--) {
      long end = boxes[i];
      if (end != MIN_VALUE) {
        if (end <= time - window) {
          total -= boxSize(i);
          boxes[i] = MIN_VALUE;
        } else {
          last = boxSize(i);
          return;
        }
      }
    }
    last = 0;
  }

  private int min(int boxSize) {
    if (boxSize == 1) {
      return 0;
    } else {
      return (Integer.numberOfTrailingZeros(boxSize) * mergeThreshold) + 1;
    }
  }
  
  private int max(int boxSize) {
    if (boxSize == 1) {
      return mergeThreshold + 1; //1-size boxes have bigger merge thresholds?!
    } else {
      return min(boxSize) + mergeThreshold;
    }
  }
  
  private int boxSize(int index) {
    if (index < max(1)) {
      return 1;
    } else {
      return 1 << ((index - 1) / mergeThreshold);
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
    
    ExponentialHistogram other = new ExponentialHistogram(mergeThreshold, window);
    this.boxes = new long[max(1)];
    this.total = 0;

    for (int i = 0, size = 1; i < originalBoxes.length; i++) {
      if (i >= max(size)) {
        size <<= 1;
      }
      
      long time = originalBoxes[i];
      if (size == 1) {
        if (other.total <= fraction * (other.total + this.total)) {
          other.insert(1, time);
          other.total += 1;
        } else {
          this.insert(1, time);
          this.total += 1;
        }
      } else {
        for (int split = 0; split < 2; split++) {
          if (other.total <= fraction * (other.total + this.total)) {
            other.insert(size >> 1, time);
            other.total += size >> 1;
          } else {
            this.insert(size >> 1, time);
            this.total += size >> 1;
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
    for (int i = 0; i < boxes.length; i++) {
      long time = boxes[i];
      if (time != MIN_VALUE) {
        sb.append("[").append(boxSize(i)).append("@").append(time).append("], ");
      }
    }
    sb.delete(sb.length() - 2, sb.length());
    return sb.toString();
  }
}

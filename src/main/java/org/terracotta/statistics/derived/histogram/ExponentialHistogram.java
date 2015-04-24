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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class ExponentialHistogram {
  
  /*
   * TODO: The original paper on this data structure doesn't (by my reading) say
   * anything about different merge thresholds for the 1-size boxes.  Need to
   * figure out if this is an error in the BASH/BSBH paper, or is an important
   * correction from the EH one.
   */
  
  private static final Comparator<Box> MERGE_COMPARATOR = new Comparator<Box>() {

    @Override
    public int compare(Box o1, Box o2) {
      int sizeDiff = Long.compare(o1.count(), o2.count());
      if (sizeDiff == 0) {
        return Long.compare(o2.end(), o1.end());
      } else {
        return sizeDiff;
      }
    }
  };
  
  private final List<Box> boxes;
  private final int mergeThreshold;
  private final long window;
  
  private long total;
  
  public ExponentialHistogram(float epsilon, long window) {
    this((int) (Math.ceil(Math.ceil(1 / epsilon) / 2) + 1), window);
  }
  
  private ExponentialHistogram(int mergeThreshold, long window) {
    this.mergeThreshold = mergeThreshold;
    this.window = window;
    this.boxes = new ArrayList<Box>(1);
  }
  
  public ExponentialHistogram(ExponentialHistogram a, ExponentialHistogram b) {
    this.mergeThreshold = a.mergeThreshold;
    this.window = a.window;
    this.total = a.total + b.total;
    this.boxes = new ArrayList<Box>(a.boxes.size() + b.boxes.size());
    
    boxes.addAll(a.boxes);
    boxes.addAll(b.boxes);
    Collections.sort(boxes, MERGE_COMPARATOR);
    
    int i = 0;
    while (i < boxes.size() && boxes.get(i).count == 1) i++;
    while (i > (mergeThreshold + 1)) {
      i -= 2;
      Box ba = boxes.remove(i);
      Box bb = boxes.remove(i);
      boxes.add(i, ba.merge(bb));
    }
      
    Collections.sort(boxes, MERGE_COMPARATOR);
    
    for (int mergeLevel = 2; i < boxes.size(); mergeLevel <<= 1) {
      int firstIndex = i;
      while (i < boxes.size() && boxes.get(i).count == mergeLevel) i++;
      while (i - firstIndex > mergeThreshold) {
        i -= 2;
        Box ba = boxes.remove(i);
        Box bb = boxes.remove(i);
        boxes.add(i, ba.merge(bb));
      }
      Collections.sort(boxes, MERGE_COMPARATOR);
    }
  }
  
  public void insert(long time) {
    merge();
    boxes.add(0, new Box(time));
    total++;
  }
  
  public void insertAndExpire(long time) {
    expire(time);
    insert(time);
  }
  
  public void expire(long time) {
    for (int i = boxes.size() - 1; i >= 0; i--) {
      if (boxes.get(i).end <= time - window) {
        total -= boxes.remove(i).count();
      } else {
        break;
      }
    }
  }
  
  public boolean isEmpty() {
    return boxes.isEmpty();
  }

  public long count() {
    if (isEmpty()) {
      return 0;
    } else {
      return total - (boxes.get(boxes.size() - 1).count() >> 1);
    }
  }
  
  public ExponentialHistogram split() {
    ExponentialHistogram other = new ExponentialHistogram(mergeThreshold, window);
    
    long thisSize = 0;
    long otherSize = 0;
    for (ListIterator<Box> it = boxes.listIterator(); it.hasNext(); ) {
      Box b = it.next();
      
      it.remove();
      for (Box split : b.split()) {
        if (otherSize <= thisSize) {
          other.boxes.add(split);
          otherSize += split.count();
        } else {
          it.add(split);
          thisSize += split.count();
        }
      }
    }
    other.total = otherSize;
    this.total = thisSize;
    other.merge();
    merge();
    return other;
  }
  
  public ExponentialHistogram split(float fraction) {
    ExponentialHistogram other = new ExponentialHistogram(mergeThreshold, window);
    
    long thisSize = 0;
    long otherSize = 0;
    for (ListIterator<Box> it = boxes.listIterator(); it.hasNext(); ) {
      Box b = it.next();
      
      it.remove();
      for (Box split : b.split()) {
        if (otherSize  <= fraction * (otherSize + thisSize)) {
          other.boxes.add(split);
          otherSize += split.count();
        } else {
          it.add(split);
          thisSize += split.count();
        }
      }
    }
    other.total = otherSize;
    this.total = thisSize;
    other.merge();
    merge();
    return other;
  }
  
  private void merge() {
    for (int i = 0, mergeLevel = 1; i < boxes.size() - mergeThreshold; mergeLevel <<= 1) {
      for (int levelIndex = 0; levelIndex <= mergeThreshold; levelIndex++, i++) {
        if (boxes.get(i).count() != mergeLevel) {
          return;
        }
      }
      i -= 2;
      Box a = boxes.remove(i);
      Box b = boxes.remove(i);
      boxes.add(i, a.merge(b));
    }
  }
  
  @Override
  public String toString() {
    return "count = " + count() + " : " + boxes.toString();
  }

  static class Box {

    private final long end;
    private final long count;

    public Box(long time) {
      this(time, 1);
    }
    
    private Box(long time, long count) {
      this.end = time;
      this.count = count;
    }
    
    public long count() {
      return count;
    }
    
    public long end() {
      return end;
    }
    
    public Box merge(Box box) {
      if (box.count() == count()) {
        return new Box(Math.max(end, box.end), count * 2);
      } else {
        throw new IllegalArgumentException("Box counts unequal");
      }
    }
    
    @Override
    public String toString() {
      return "[" + count() + "@" + end() + "]";
    }

    private Box[] split() {
      if (count() == 1) {
        return new Box[] {this};
      } else {
        return new Box[] {new Box(end, count >> 1), new Box(end, count >> 1)};
      }
    }
  }
}

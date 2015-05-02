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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.terracotta.statistics.derived.histogram.Histogram.Bucket;

import static java.lang.Long.max;
import static java.lang.Long.min;

public class BarSplittingHistogram implements Histogram<Double> {
  
  private final float maxCoefficient;
  private final float barEpsilon;
  private final long window;
  private final int barCount;
  private final int bucketCount;
  private final List<Bar> bars;

  public BarSplittingHistogram(float maxCoefficient, int expansionFactor, int bucketCount, float barEpsilon, long window) {
    this.maxCoefficient = maxCoefficient;
    this.bucketCount = bucketCount;
    this.barCount = bucketCount * expansionFactor;
    this.bars = new ArrayList<Bar>(barCount);
    this.barEpsilon = barEpsilon;
    this.window = window;
  }

  public BarSplittingHistogram(int bucketCount, long window) {
    this(1.7f, 7, bucketCount, 0.1f, window);
  }

  public void event(long value, long time) {
    Bar bar = getBar(value);
    bar.insert(value, time);
    if (bar.count() > maxBarSize()) {
      split(bar);
    }
    for (Bar b : bars) {
      b.expire(time);
    }
  }

  @Override
  public String toString() {
    return bars.toString();
  }
  
  @Override
  public List<Bucket<Double>> getBuckets() {
    List<Bucket<Double>> buckets = new ArrayList<Bucket<Double>>(bucketCount);
    double targetSize = ((double) size()) / bucketCount;
    Iterator<Bar> it = bars.iterator();
    Bar b = it.next();
    double minimum = b.minimum();
    double count = b.count();
    for (int i = 0; i < bucketCount; i++) {
      while (count < targetSize) {
        count += (b = it.next()).count();
      }
      
      double surplus = count - targetSize;
      double maximum = b.minimum() + ((b.maximum() - b.minimum()) * (1 - surplus/b.count()));
      buckets.add(new ImmutableDoubleBucket(minimum, maximum, targetSize));
      minimum = maximum;
      count = surplus;
    }
    return buckets;
  }
  
  private long maxBarSize() {
    return (long) (maxCoefficient * (size() / barCount));
  }
  
  private void split(Bar x) {
    if (bars.size() == barCount && !mergeBars()) {
      return;
    }
    bars.add(bars.indexOf(x) + 1, x.split());
  }

  private boolean mergeBars() {
    int lowestAggregateIndex = -1;
    long lowestAggregate = Long.MAX_VALUE;
    int lowestEmptyAdjacentIndex = -1;
    long lowestEmptyAdjacent = Long.MAX_VALUE;
    ListIterator<Bar> it = bars.listIterator();
    if (!it.hasNext()) {
      return false;
    }
    int currentIndex = it.nextIndex();
    Bar current = it.next();
    while (it.hasNext()) {
      int nextIndex = it.nextIndex();
      Bar next = it.next();
      if (current.isEmpty()) {
        if (next.isEmpty()) {
          //Priority #1: two adjacent empty bars
          bars.remove(currentIndex);
          bars.remove(currentIndex);
          return true;
        } else {
          //current == 0, next != 0
          if (next.count() < lowestEmptyAdjacent) {
            lowestEmptyAdjacent = next.count();
            lowestEmptyAdjacentIndex = currentIndex;
          }
        }
      } else {
        if (next.isEmpty()) {
          //next == 0, current != 0
          if (current.count() < lowestEmptyAdjacent) {
            lowestEmptyAdjacent = current.count();
            lowestEmptyAdjacentIndex = nextIndex;
          }
        } else {
          //next != 0, current != 0
          long aggregate = current.count() + next.count();
          if (aggregate < lowestAggregate) {
            lowestAggregate = aggregate;
            lowestAggregateIndex = currentIndex;
          }
        }
      }
      currentIndex = nextIndex;
      current = next;
    }
    
    if (lowestEmptyAdjacentIndex >= 0) {
      //Priority #2: bar with adjacent empty bar
      bars.remove(lowestEmptyAdjacentIndex);
      return true;
    } else if (lowestAggregateIndex >= 0 && lowestAggregate < maxBarSize()) {
      //Priority #3: two adjacent bars with minimum aggregate size (< maxSize)
      Bar a = bars.remove(lowestAggregateIndex);
      Bar b = bars.remove(lowestAggregateIndex);
      bars.add(lowestAggregateIndex, a.merge(b));
      return true;
    } else {
      return false;
    }
  }

  private Bar getBar(long value) {
    if (bars.isEmpty()) {
      Bar bar = new Bar(barEpsilon, window);
      bars.add(bar);
      return bar;
    } else {
      return searchForBar(value);
    }
  }

  private Bar searchForBar(long value) {
    int low = 0;
    int high = bars.size() - 1;

    Bar bar = null;
    while (low <= high) {
        int mid = (low + high) >>> 1;
        bar = bars.get(mid);
        int cmp = bar.compareTo(value);

        if (cmp < 0)
            low = mid + 1;
        else if (cmp > 0)
            high = mid - 1;
        else
            return bar;
    }
    return bar;
  }
  
  private long size() {
    long size = 0;
    for (Bar b : bars) {
      size += b.count();
    }
    return size;
  }

  static class Bar {
    
    private final ExponentialHistogram eh;
    private long minimum = Long.MAX_VALUE;
    private long maximum = Long.MIN_VALUE;
    
    public Bar(float epsilon, long window) {
      this.eh = new ExponentialHistogram(epsilon, window);
    }

    private Bar(ExponentialHistogram eh, long minimum, long maximum) {
      this.eh = eh;
      this.minimum = minimum;
      this.maximum = maximum;
    }

    public void insert(long value, long time) {
      minimum = Math.min(minimum, value);
      maximum = Math.max(maximum, value + 1);
      eh.insert(time);
    }

    public void expire(long time) {
      eh.expire(time);
    }

    public boolean isEmpty() {
      return eh.isEmpty();
    }
    
    public long count() {
      return eh.count();
    }

    public int compareTo(long value) {
      if (value >= maximum) {
        return -1;
      } if (value < minimum) {
        return 1;
      } else {
        return 0;
      }
    }

    @Override
    public String toString() {
      return "[" + minimum + " --" + count() + "-> " + maximum + "]";
    }

    public Bar split() {
      ExponentialHistogram split = eh.split(0.5f);
      long upperMinimum = (minimum + maximum) / 2;
      long upperMaximum = maximum;
      this.maximum = upperMinimum;
      return new Bar(split, upperMinimum, upperMaximum);
    }

    public Bar merge(Bar b) {
      return new Bar(new ExponentialHistogram(eh, b.eh), min(minimum, b.minimum), max(maximum, b.maximum));
    }

    public long minimum() {
      return minimum;
    }
    
    public long maximum() {
      return maximum;
    }
  }
}

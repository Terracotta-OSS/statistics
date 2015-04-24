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

import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BarSplittingBiasedHistogram implements Histogram<Double> {
  
  private final double maxCoefficient;
  private final float barEpsilon;
  private final long window;
  private final int barCount;
  private final int bucketCount;
  private final double phi;
  private final double alphaPhi;
  private final double rho;
  private final double alphaRho;
  private final List<Bar> bars;
  
  public BarSplittingBiasedHistogram(double maxCoefficient, double phi, int expansionFactor, int bucketCount, float barEpsilon, long window) {
    this.maxCoefficient = maxCoefficient;
    this.bucketCount = bucketCount;
    this.barCount = bucketCount * expansionFactor;
    
    this.bars = new ArrayList<Bar>(barCount);
    this.barEpsilon = barEpsilon;
    this.window = window;
    this.phi = phi;
    this.alphaPhi = (phi == 1.0f) ? 1.0 / bucketCount : (1 - phi) / (1 - Math.pow(phi, bucketCount));
    this.rho = Math.pow(phi, 1.0 / expansionFactor);
    this.alphaRho = (rho == 1.0f) ? 1.0 / barCount : (1 - rho) / (1 - Math.pow(rho, barCount));
  }

  public BarSplittingBiasedHistogram(int bucketCount, long window) {
    this(1.7f, 0.7f, 7, bucketCount, 0.1f, window);
  }

  public BarSplittingBiasedHistogram(double phi, int bucketCount, long window) {
    this(1.7f, phi, 7, bucketCount, 0.1f, window);
  }
  
  public void event(long value, long time) {
    int barIndex = getBarIndex(value);
    Bar bar = bars.get(barIndex);
    bar.insert(value, time);
    if (bar.count() > maxBarSize(barIndex)) {
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
  public List<Histogram.Bucket<Double>> getBuckets() {
    List<Histogram.Bucket<Double>> buckets = new ArrayList<Histogram.Bucket<Double>>(bucketCount);
    double targetSize = size() * alphaPhi;
    Iterator<Bar> it = bars.iterator();
    Bar b = it.next();
    double minimum = b.minimum();
    double count = b.count();
    for (int i = 0; i < bucketCount; i++) {
      while (count < targetSize && it.hasNext()) {
        count += (b = it.next()).count();
      }
      
      double surplus = count - targetSize;
      double maximum = b.minimum() + (b.maximum() - b.minimum()) * (1 - surplus/b.count());
      buckets.add(new ImmutableDoubleBucket(minimum, maximum, targetSize));
      minimum = maximum;
      count = surplus;
      targetSize *= phi;
    }
    return buckets;
  }

  public double[] getQuantileBounds(double quantile) {
    double threshold = quantile * size();
    long count = 0;
    for (Bar b : bars) {
      count += b.count();
      if (count >= threshold) {
        return new double[] {b.minimum(), b.maximum()};
      }
    }
    throw new AssertionError();
  }
  private long maxBarSize(int barIndex) {
    return max(1L, (long) (maxCoefficient * size() * alphaRho * Math.pow(rho, barIndex)));
  }

  private void split(Bar x) {
    if (bars.size() == barCount && !mergeBars()) {
      return;
    }
    bars.add(bars.indexOf(x) + 1, x.split(rho));
  }

  private boolean mergeBars() {
    int lowestAggregateIndex = -1;
    double lowestAggregate = Double.POSITIVE_INFINITY;
    ListIterator<Bar> it = bars.listIterator();
    if (!it.hasNext()) {
      return false;
    }
    int currentIndex = it.nextIndex();
    Bar current = it.next();
    while (it.hasNext()) {
      int nextIndex = it.nextIndex();
      Bar next = it.next();
      if (current.isEmpty() && next.isEmpty()) {
        //Priority #1: two adjacent empty bars
        bars.remove(currentIndex);
        bars.remove(currentIndex);
        return true;
      } else {
        //next != 0, current != 0
        double aggregate = (((double) current.count()) / Math.pow(rho, currentIndex)) + (((double) next.count()) / Math.pow(rho, nextIndex));
        if (aggregate < lowestAggregate) {
          lowestAggregate = aggregate;
          lowestAggregateIndex = currentIndex;
        }
      }
      currentIndex = nextIndex;
      current = next;
    }
    
    if (lowestAggregateIndex >= 0 && (bars.get(lowestAggregateIndex).count() + bars.get(lowestAggregateIndex + 1).count() < maxBarSize(lowestAggregateIndex))) {
      //Priority #2: two adjacent bars with minimum aggregate size (< maxSize)
      Bar a = bars.remove(lowestAggregateIndex);
      Bar b = bars.remove(lowestAggregateIndex);
      bars.add(lowestAggregateIndex, a.merge(b));
      return true;
    } else {
      return false;
    }
  }
  
  private int getBarIndex(long value) {
    if (bars.isEmpty()) {
      Bar bar = new Bar(barEpsilon, window);
      bars.add(bar);
      return 0;
    } else {
      return searchForBarIndex(value);
    }
  }

  private int searchForBarIndex(long value) {
    int low = 0;
    int high = bars.size() - 1;

    int mid = 0;
    while (low <= high) {
        mid = (low + high) >>> 1;
        Bar bar = bars.get(mid);
        int cmp = bar.compareTo(value);

        if (cmp < 0)
            low = mid + 1;
        else if (cmp > 0)
            high = mid - 1;
        else
            return mid;
    }
    return mid;
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
    private double minimum = Double.POSITIVE_INFINITY;
    private double maximum = Double.NEGATIVE_INFINITY;

    public Bar(float epsilon, long window) {
      this.eh = new ExponentialHistogram(epsilon, window);
    }

    private Bar(ExponentialHistogram eh, double minimum, double maximum) {
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

    public Bar split(double rho) {
      ExponentialHistogram split = eh.split((float) (1.0 / (1.0 + rho)));
      double upperMinimum = (minimum + (rho * maximum)) / (1.0 + rho);
      double upperMaximum = maximum;
      this.maximum = upperMinimum;
      
      return new Bar(split, upperMinimum, upperMaximum);
    }

    public Bar merge(Bar b) {
      return new Bar(new ExponentialHistogram(eh, b.eh), min(minimum, b.minimum), max(maximum, b.maximum));
    }

    public double minimum() {
      return minimum;
    }
    
    public double maximum() {
      return maximum;
    }
  }
  
}

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.nextUp;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Stream.of;

/**
 * An implementation of the histogram algorithm described in:
 * 'Fast Computation of Approximate Biased Histograms on Sliding Windows over Data Streams' [H. Mousavi &amp; C. Zaniolo]
 *
 * @see <a href="http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.407.3977">
 *   Fast Computation of Approximate Biased Histograms on Sliding Windows over Data Streams</a>
 */
public class BarSplittingBiasedHistogram implements Histogram<Double> {
  private static final double DEFAULT_MAX_COEFFICIENT = 1.7;
  private static final double DEFAULT_PHI = 0.7;
  private static final int DEFAULT_EXPANSION_FACTOR = 7;
  private static final double DEFAULT_EXP_HISTOGRAM_EPSILON = 0.01;

  private final int barCount;
  private final int bucketCount;
  private final double phi;
  private final double alphaPhi;
  private final double ratio;
  private final List<Bar> bars;
  private final double[] maxSizeTable;
  
  private long size;

  /**
   * Create a histogram maintained over a sliding time window.
   * <p>
   *   The constructed histogram is:
   * </p>
   * <ul>
   *   <li>maintained over {@code window} sliding window</li>
   *   <li>consists of {@code bucketCount} buckets</li>
   *   <li>where {@code b1.size() ~= b0.size * phi}</li>
   *   <li>with each bucket internally composed of {@code expansionFactor} bars</li>
   *   <li>with each bar maintaining a count accurate with a fractional error of {@code barEpsilon}</li>
   *   <li>where bars are split when there size exceeds {@code maxCoefficient} of their target size</li>
   * </ul>
   *
   * @param maxCoefficient relative split threshold
   * @param phi histogram bucket bias factor
   * @param expansionFactor number of bars per bucket
   * @param bucketCount number of buckets
   * @param barEpsilon bar count relative error
   * @param window sliding window size
   */
  public BarSplittingBiasedHistogram(double maxCoefficient, double phi, int expansionFactor, int bucketCount, double barEpsilon, long window) {
    this.bucketCount = bucketCount;
    this.barCount = bucketCount * expansionFactor;
    
    this.bars = new ArrayList<Bar>(barCount);
    this.bars.add(new Bar(barEpsilon, window));
    this.phi = phi;

    /*
     * Using L'Hôpital: lim_(x->1) f(x)/g(x) = lim_(x->1)(f'(x))/(g'(x))
     * So: lim_(phi->1) (1-phi)/(1-phi^n) = lim_(phi->1) 1/n
     */
    this.alphaPhi = (phi == 1.0) ? 1.0 / bucketCount : (1 - phi) / (1 - Math.pow(phi, bucketCount));

    double rho = Math.pow(phi, 1.0 / expansionFactor);
    double alphaRho = (rho == 1.0f) ? 1.0 / barCount : (1 - rho) / (1 - Math.pow(rho, barCount));
    this.ratio = (rho / (1.0 + rho));
    this.maxSizeTable = new double[barCount];
    for (int i = 0; i < barCount; i++) {
      this.maxSizeTable[i] = maxCoefficient * alphaRho * Math.pow(rho, i);
    }
  }

  /**
   * Create a histogram maintained over a sliding time window.
   * <p>
   *   The constructed histogram is:
   * </p>
   * <ul>
   *   <li>maintained over {@code window} sliding window</li>
   *   <li>consists of {@code bucketCount} buckets</li>
   *   <li>where {@code b1.size() ~= b0.size * 0.7}</li>
   * </ul>
   *
   * @param bucketCount number of buckets
   * @param window sliding window size
   */
  public BarSplittingBiasedHistogram(int bucketCount, long window) {
    this(DEFAULT_MAX_COEFFICIENT, DEFAULT_PHI, DEFAULT_EXPANSION_FACTOR, bucketCount, DEFAULT_EXP_HISTOGRAM_EPSILON, window);
  }

  /**
   * Create a histogram maintained over a sliding time window.
   * <p>
   *   The constructed histogram is:
   * </p>
   * <ul>
   *   <li>maintained over {@code window} sliding window</li>
   *   <li>consists of {@code bucketCount} buckets</li>
   *   <li>where {@code b1.size() ~= b0.size * phi}</li>
   * </ul>
   *
   * @param phi histogram bucket bias factor
   * @param bucketCount number of buckets
   * @param window sliding window size
   */
  public BarSplittingBiasedHistogram(double phi, int bucketCount, long window) {
    this(DEFAULT_MAX_COEFFICIENT, phi, DEFAULT_EXPANSION_FACTOR, bucketCount, DEFAULT_EXP_HISTOGRAM_EPSILON, window);
  }

  /**
   * Record an event of the given {@code value} occuring at he given {@code time}
   *
   * @param value event value
   * @param time event time
   */
  public void event(double value, long time) {
    int barIndex = getBarIndex(value);
    Bar bar = bars.get(barIndex);
    long before = bar.count();
    bar.insert(value, time);
    long after = bar.count();
    size += (after - before);
    if (after > maxBarSize(barIndex)) {
      split(bar, barIndex);
    }
  }

  /**
   * Expire old events from all buckets.
   *
   * @param time current timestamp
   */
  public void expire(long time) {
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
    double targetSize = size() * alphaPhi; // * phi^0
    Iterator<Bar> it = bars.iterator();
    Bar b = it.next();
    double minimum = b.minimum();
    double count = b.count();
    for (int i = 0; i < bucketCount - 1; i++) {
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
    while (it.hasNext()) {
      count += (b = it.next()).count();
    }
    buckets.add(new ImmutableDoubleBucket(minimum, b.maximum(), count));
    return buckets;
  }

  @Override
  public Double[] getQuantileBounds(double quantile) {
    if (quantile > 1.0 || quantile < 0.0) {
      throw new IllegalArgumentException("Invalid quantile requested: " + quantile);
    } else {
      return of(evaluateQuantileFromMin(quantile), evaluateQuantileFromMax(quantile))
          .min(comparingDouble(bounds -> bounds[1] - bounds[0])).get();
    }
  }

  private Double[] evaluateQuantileFromMax(double quantile) {
    double threshold = (1.0 - quantile) * size();

    double lowCount = 0;
    double highCount = 0;

    for (ListIterator<Bar> it = bars.listIterator(bars.size()); it.hasPrevious(); ) {
      Bar b = it.previous();
      lowCount += b.count() * (1.0 - b.epsilon());
      highCount += b.count() * (1.0 + b.epsilon());

      if (highCount >= threshold) {
        double upperBound = b.maximum();
        while (lowCount < threshold && it.hasPrevious()) {
          b = it.previous();
          lowCount += b.count() * (1.0 - b.epsilon());
        }
        return new Double[] {b.minimum(), upperBound};
      }
    }
    throw new AssertionError();
  }

  private Double[] evaluateQuantileFromMin(double quantile) {
    double threshold = quantile * size();

    double lowCount = 0;
    double highCount = 0;

    for (ListIterator<Bar> it = bars.listIterator(); it.hasNext(); ) {
      Bar b = it.next();
      lowCount += b.count() * (1.0 - b.epsilon());
      highCount += b.count() * (1.0 + b.epsilon());

      if (highCount >= threshold) {
        double lowerBound = b.minimum();
        while (lowCount < threshold && it.hasNext()) {
          b = it.next();
          lowCount += b.count() * (1.0 - b.epsilon());
        }
        return new Double[] {lowerBound, b.maximum()};
      }
    }
    throw new AssertionError();
  }

  private double maxBarSize(int barIndex) {
    return size() * maxSizeTable[barIndex];
  }

  private void split(Bar x, int xIndex) {
    int mergePoint = Integer.MAX_VALUE;
    if (bars.size() < barCount || (mergePoint = mergeBars()) >= 0) {
      long before = x.count();
      Bar split = x.split(ratio);
      size += (x.count() + split.count()) - before;

      if (xIndex < mergePoint) {
        bars.add(xIndex + 1, split);
      } else if (xIndex > mergePoint) {
        bars.add(xIndex, split);
      }
    }
  }

  private int mergeBars() {
    int lowestAggregateIndex = -1;
    double lowestAggregate = Double.POSITIVE_INFINITY;

    for (int index = 0; index < bars.size() - 1; index++) {
      Bar current = bars.get(index);
      Bar next = bars.get(index + 1);
      double aggregate = (((double) current.count()) / maxSizeTable[index]) + (((double) next.count()) / maxSizeTable[index + 1]);
      if (aggregate < lowestAggregate) {
        lowestAggregate = aggregate;
        lowestAggregateIndex = index;
      }
    }
    
    if (bars.get(lowestAggregateIndex).count() + bars.get(lowestAggregateIndex + 1).count() < maxBarSize(lowestAggregateIndex)) {
      Bar upper = bars.remove(lowestAggregateIndex + 1);
      Bar lower = bars.get(lowestAggregateIndex);
      long before = lower.count() + upper.count();
      lower.merge(upper);
      size += lower.count() - before;
      return lowestAggregateIndex + 1;
    } else {
      return -1;
    }
  }
  
  private int getBarIndex(double value) {
    int low = 0;
    int high = bars.size() - 1;

    int mid;
    do {
      mid = (high + low) >>> 1;
      Bar bar = bars.get(mid);
      if (value >= bar.maximum()) {
        low = mid + 1;
      } else if (value < bar.minimum()) {
        high = mid - 1;
      } else {
        return mid;
      }
    } while (low <= high);
    
    return mid;
  }
  
  private long size() {
    return size;
  }

  private static final class Bar {
    
    private final ExponentialHistogram eh;
    private double minimum = Double.POSITIVE_INFINITY;
    private double maximum = Double.NEGATIVE_INFINITY;

    Bar(double epsilon, long window) {
      this.eh = new ExponentialHistogram(epsilon, window);
    }

    private Bar(ExponentialHistogram eh, double minimum, double maximum) {
      this.eh = eh;
      this.minimum = minimum;
      this.maximum = maximum;
    }

    void insert(double value, long time) {
      if (value < minimum) {
        minimum = value;
      }
      if (value >= maximum) {
        maximum = nextUp(value);
      }
      eh.insert(time);
    }

    void expire(long time) {
      eh.expire(time);
    }

    long count() {
      return eh.count();
    }

    @Override
    public String toString() {
      return "[" + minimum + " --" + count() + "-> " + maximum + "]";
    }

    Bar split(double ratio) {
      ExponentialHistogram split = eh.split(ratio);
      double upperMinimum = minimum + ((maximum - minimum) * ratio);
      double upperMaximum = maximum;
      this.maximum = upperMinimum;
      
      return new Bar(split, upperMinimum, upperMaximum);
    }

    void merge(Bar higher) {
      eh.merge(higher.eh);
      maximum = higher.maximum;
    }

    double minimum() {
      return minimum;
    }

    double maximum() {
      return maximum;
    }

    double epsilon() {
      return eh.epsilon();
    }
  }
  
}

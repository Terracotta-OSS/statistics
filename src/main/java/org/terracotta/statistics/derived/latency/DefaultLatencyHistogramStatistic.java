/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.statistics.derived.latency;

import org.terracotta.statistics.Time;
import org.terracotta.statistics.derived.histogram.BarSplittingBiasedHistogram;
import org.terracotta.statistics.derived.histogram.Histogram;
import org.terracotta.statistics.observer.ChainedEventObserver;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongSupplier;

import static java.lang.Math.nextDown;

/**
 * @author Mathieu Carbou
 */
public class DefaultLatencyHistogramStatistic implements LatencyHistogramStatistic, ChainedEventObserver {

  private final BarSplittingBiasedHistogram histogram;
  private final LongSupplier timeSupplier;
  private final long pruningDelay;

  // histogram querying without any expire call
  private final LatencyHistogramQuery query = new LatencyHistogramQuery() {
    @Override
    public Long minimum() {
      return nullOrVal(histogram.getMinimum());
    }

    @Override
    public Long maximum() {
      return nullOrVal(histogram.getMaximum());
    }

    @Override
    public long count() {
      return histogram.size();
    }

    @Override
    public Long percentile(double percent) {
      return nullOrVal(percent == 0.0 ? histogram.getMinimum() : nextDown(histogram.getQuantileBounds(percent)[1]));
    }

    @Override
    public long[] percentileBounds(double percent) {
      if (percent == 0.0) {
        double v = histogram.getMinimum();
        return Double.isNaN(v) ? null : new long[]{(long) v, (long) v};
      }
      double[] bounds = histogram.getQuantileBounds(percent);
      if (Double.isNaN(bounds[0]) || Double.isNaN(bounds[1])) {
        return null;
      }
      return new long[]{(long) bounds[0], (long) nextDown(bounds[1])};
    }

    @Override
    public List<Histogram.Bucket> buckets() {
      return histogram.getBuckets();
    }
  };

  private long nextPruning;

  /**
   * Create a histogram maintained over a sliding time window.
   * <p>
   * The constructed histogram is:
   * </p>
   * <ul>
   * <li>maintained over {@code window} sliding window</li>
   * <li>consists of {@code bucketCount} buckets</li>
   * <li>where {@code b1.size() ~= b0.size * phi}</li>
   * </ul>
   * If "phi" is high, the quantile bounds will be more precise for lower percentiles such as minimum.
   * If "phi" is low, the quantile bounds will be more precise for higher percentiles such as 99%-ile, maximum.
   *
   * @param phi         histogram bucket bias factor
   * @param bucketCount number of buckets
   * @param window      sliding window size, in ns
   * @param timeSupplier      the supplier of time, which must be in the same unit as the time passed to the {{@link #event(long, long)}} method.
   */
  public DefaultLatencyHistogramStatistic(
      double phi,
      int bucketCount,
      Duration window,
      LongSupplier timeSupplier) {
    this.timeSupplier = timeSupplier;
    this.histogram = new BarSplittingBiasedHistogram(phi, bucketCount, window.toNanos());
    this.pruningDelay = window.toNanos() / 2;
  }

  public DefaultLatencyHistogramStatistic(double phi,
                                          int bucketCount,
                                          Duration window) {
    this(phi, bucketCount, window, Time::time);
  }

  @Override
  public List<org.terracotta.statistics.derived.histogram.Histogram.Bucket> buckets() {
    return query(LatencyHistogramQuery::buckets);
  }

  @Override
  public long count() {
    return query(LatencyHistogramQuery::count);
  }

  @Override
  public Long minimum() {
    return query(LatencyHistogramQuery::minimum);
  }

  @Override
  public Long maximum() {
    return query(LatencyHistogramQuery::maximum);
  }

  @Override
  public Long percentile(double percent) {
    return query(h -> h.percentile(percent));
  }

  @Override
  public long[] percentileBounds(double percent) {
    return query(h -> h.percentileBounds(percent));
  }

  @Override
  public synchronized void event(long time, long latency) {
    histogram.event(latency, time);
    tryExpire(false, () -> time);
  }

  @Override
  public synchronized <T> T query(Function<LatencyHistogramQuery, T> fn) {
    tryExpire(true, timeSupplier);
    return fn.apply(query);
  }

  @Override
  public String toString() {
    return query(query -> "{" +
        "count=" + query.count() +
        ", minimum=" + query.minimum() +
        ", maximum=" + query.maximum() +
        ", median=" + query.median() +
        '}');
  }

  // Expire the histogram if it is time to expire it, or if force is true AND it is dirty
  private void tryExpire(boolean force, LongSupplier time) {
    long now = time.getAsLong();
    if (force || now >= nextPruning) {
      nextPruning = now + pruningDelay;
      histogram.expire(now);
    }
  }

  private static Long nullOrVal(double val) {
    return Double.isNaN(val) ? null : (long) val;
  }

}

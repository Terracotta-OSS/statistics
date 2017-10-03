/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics.extended;

import org.terracotta.statistics.OperationStatistic;
import org.terracotta.statistics.archive.Timestamped;
import org.terracotta.statistics.derived.EventRateSimpleMovingAverage;
import org.terracotta.statistics.derived.OperationResultFilter;
import org.terracotta.statistics.observer.ChainedOperationObserver;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Class RateStatistic.
 *
 * @param <T> the generic type
 * @author cdennis
 */
public class RateImpl<T extends Enum<T>> implements SampledStatistic<Double> {

  private final ExpiringSampledStatistic<Double> delegate;

  /**
   * The rate.
   */
  private final EventRateSimpleMovingAverage rate;

  /**
   * Instantiates a new rate statistic.
   *
   * @param source        the source event to monitor
   * @param targets       the targets
   * @param averagePeriod the average period
   * @param averageTimeUnit average period unit
   * @param executor      the executor
   * @param historySize   the history size
   * @param historyPeriod the history period
   * @param historyTimeUnit history period unit
   */
  public RateImpl(final OperationStatistic<T> source, final Set<T> targets, long averagePeriod, TimeUnit averageTimeUnit,
                  ScheduledExecutorService executor, int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    this.rate = new EventRateSimpleMovingAverage(averagePeriod, averageTimeUnit);
    this.delegate = new ExpiringSampledStatistic<Double>(rate, executor, historySize, historyPeriod, historyTimeUnit, StatisticType.RATE) {

      private final ChainedOperationObserver<T> observer = new OperationResultFilter<>(targets, rate);

      @Override
      protected void stopStatistic() {
        super.stopStatistic();
        source.removeDerivedStatistic(observer);
      }

      @Override
      protected void startStatistic() {
        super.startStatistic();
        source.addDerivedStatistic(observer);
      }
    };
  }

  @Override
  public boolean active() {
    return delegate.active();
  }

  @Override
  public Double value() {
    return delegate.value();
  }

  @Override
  public List<Timestamped<Double>> history() {
    return delegate.history();
  }

  @Override
  public List<Timestamped<Double>> history(long since) {
    return delegate.history(since);
  }

  @Override
  public StatisticType type() {
    return StatisticType.RATE;
  }

  /**
   * Start sampling.
   */
  protected void start() {
    delegate.start();
  }

  /**
   * Sets the window.
   *
   * @param averagePeriod   the new window
   * @param averageTimeUnit the new window's time unit
   */
  protected void setWindow(long averagePeriod, TimeUnit averageTimeUnit) {
    rate.setWindow(averagePeriod, averageTimeUnit);
  }

  /**
   * Set the sample history parameters.
   *
   * @param historySize     history sample size
   * @param historyPeriod   history sample period
   * @param historyTimeUnit history time unit
   */
  protected void setHistory(int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    delegate.setHistory(historySize, historyPeriod, historyTimeUnit);
  }

  /**
   * Check the statistic for expiry.
   *
   * @param expiry expiry threshold
   * @return {@code true} if expired
   */
  protected boolean expire(long expiry) {
    return delegate.expire(expiry);
  }
}

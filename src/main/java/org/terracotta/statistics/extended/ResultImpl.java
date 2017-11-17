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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.terracotta.statistics.OperationStatistic;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Class OperationImpl.
 *
 * @param <T> the generic type
 * @author cdennis
 */
class ResultImpl<T extends Enum<T>> implements Result {

  /**
   * The count.
   */
  private final SemiExpiringSampledStatistic<Long> count;

  /**
   * The rate.
   */
  private final RateImpl<T> rate;

  /**
   * The latency.
   */
  private final LatencyImpl<T> latency;

  /**
   * Instantiates a new operation impl.
   *
   * @param source        the source
   * @param targets       the targets
   * @param averagePeriod the average period
   * @param executor      the executor
   * @param historySize   the history size
   * @param historyPeriod the history period
   */
  public ResultImpl(OperationStatistic<T> source, Set<T> targets, long averagePeriod, TimeUnit averageTimeUnit,
                    ScheduledExecutorService executor, int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    this.count = new SemiExpiringSampledStatistic<>(source.statistic(targets), executor, historySize, historyPeriod, historyTimeUnit);
    this.latency = new LatencyImpl<>(source, targets, averagePeriod, averageTimeUnit, executor, historySize, historyPeriod, historyTimeUnit);
    this.rate = new RateImpl<>(source, targets, averagePeriod, averageTimeUnit, executor, historySize, historyPeriod, historyTimeUnit);
  }

  @Override
  public SampledStatistic<Double> rate() {
    return rate;
  }

  @Override
  public Latency latency() throws UnsupportedOperationException {
    return latency;
  }

  @Override
  public SampledStatistic<Long> count() {
    return count;
  }

  /**
   * Start.
   */
  void start() {
    count.start();
    rate.start();
    latency.start();
  }

  /**
   * Expire.
   *
   * @param expiryTime the expiry time
   * @return true, if successful
   */
  @SuppressFBWarnings("NS_DANGEROUS_NON_SHORT_CIRCUIT")
  boolean expire(long expiryTime) {
    // Not using && on purpose here. expire() has a side-effect. We want to make sure all of them are called (no short-circuit evaluation)
    return count.expire(expiryTime) & rate.expire(expiryTime) & latency.expire(expiryTime);
  }

  /**
   * Sets the window.
   *
   * @param averagePeriod   the new window period
   * @param averageTimeUnit the period's time unit
   */
  void setWindow(long averagePeriod, TimeUnit averageTimeUnit) {
    rate.setWindow(averagePeriod, averageTimeUnit);
    latency.setWindow(averagePeriod, averageTimeUnit);
  }

  /**
   * Sets the history.
   *
   * @param historySize   the history size
   * @param historyPeriod the history period
   */
  void setHistory(int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    count.setHistory(historySize, historyPeriod, historyTimeUnit);
    rate.setHistory(historySize, historyPeriod, historyTimeUnit);
    latency.setHistory(historySize, historyPeriod, historyTimeUnit);
  }
}

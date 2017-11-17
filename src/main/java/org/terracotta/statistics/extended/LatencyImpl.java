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
import org.terracotta.statistics.SourceStatistic;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.derived.EventParameterSimpleMovingAverage;
import org.terracotta.statistics.derived.LatencySampling;
import org.terracotta.statistics.observer.ChainedOperationObserver;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Class LatencyImpl.
 *
 * @param <T> the generic type
 * @author cdennis
 */
class LatencyImpl<T extends Enum<T>> implements Latency {
  private final SourceStatistic<ChainedOperationObserver<? super T>> source;
  private final LatencySampling<T> latencySampler;
  private final EventParameterSimpleMovingAverage average;
  private final SampledStatisticImpl<Long> minimumStatistic;
  private final SampledStatisticImpl<Long> maximumStatistic;
  private final SampledStatisticImpl<Double> averageStatistic;

  private boolean active = false;
  private long touchTimestamp = -1;

  /**
   * Instantiates a new latency impl.
   *
   * @param statistic       the statistic
   * @param targets         the targets
   * @param averagePeriod   the average period
   * @param averageTimeUnit the average time unit
   * @param executor        the executor
   * @param historySize     the history size
   * @param historyPeriod   the history period
   * @param historyTimeUnit the history time unit
   */
  public LatencyImpl(OperationStatistic<T> statistic, Set<T> targets, long averagePeriod, TimeUnit averageTimeUnit,
                     ScheduledExecutorService executor, int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    this.average = new EventParameterSimpleMovingAverage(averagePeriod, averageTimeUnit);
    this.minimumStatistic = new SampledStatisticImpl<>(this, average.minimumStatistic(), executor, historySize, historyPeriod, historyTimeUnit);
    this.maximumStatistic = new SampledStatisticImpl<>(this, average.maximumStatistic(), executor, historySize, historyPeriod, historyTimeUnit);
    this.averageStatistic = new SampledStatisticImpl<>(this, average.averageStatistic(), executor, historySize, historyPeriod, historyTimeUnit);
    this.latencySampler = new LatencySampling<>(targets, 1.0);
    this.latencySampler.addDerivedStatistic(average);
    this.source = statistic;
  }

  /**
   * Start.
   */
  synchronized void start() {
    if (!active) {
      source.addDerivedStatistic(latencySampler);
      minimumStatistic.startSampling();
      maximumStatistic.startSampling();
      averageStatistic.startSampling();
      active = true;
    }
  }

  /**
   * Get the minimum
   */
  @Override
  public SampledStatistic<Long> minimum() {
    return minimumStatistic;
  }

  /**
   * Get the maximum.
   */
  @Override
  public SampledStatistic<Long> maximum() {
    return maximumStatistic;
  }

  /**
   * Get the average.
   */
  @Override
  public SampledStatistic<Double> average() {
    return averageStatistic;
  }

  synchronized void touch() {
    touchTimestamp = Time.absoluteTime();
    start();
  }

  /**
   * Expire.
   *
   * @param expiry the expiry
   * @return true, if successful
   */
  public synchronized boolean expire(long expiry) {
    if (touchTimestamp < expiry) {
      if (active) {
        source.removeDerivedStatistic(latencySampler);
        minimumStatistic.stopSampling();
        maximumStatistic.stopSampling();
        averageStatistic.stopSampling();
        active = false;
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Sets the window.
   *
   * @param averagePeriod   the new window
   * @param averageTimeUnit the new window's time unit
   */
  void setWindow(long averagePeriod, TimeUnit averageTimeUnit) {
    average.setWindow(averagePeriod, averageTimeUnit);
  }

  /**
   * Sets the history.
   *
   * @param historySize     the history size
   * @param historyPeriod   the history period
   * @param historyTimeUnit the history unit
   */
  void setHistory(int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    minimumStatistic.setHistory(historySize, historyPeriod, historyTimeUnit);
    maximumStatistic.setHistory(historySize, historyPeriod, historyTimeUnit);
    averageStatistic.setHistory(historySize, historyPeriod, historyTimeUnit);
  }

  public boolean active() {
    return active;
  }
}

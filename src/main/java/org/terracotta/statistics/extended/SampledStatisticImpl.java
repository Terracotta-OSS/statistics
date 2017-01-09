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

import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.archive.Timestamped;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Class StatisticImpl.
 */
class SampledStatisticImpl<T extends Number> extends AbstractSampledStatistic<T> {

  private final LatencyImpl latency;

  /**
   * Instantiates a new statistic impl.
   *
   * @param value           the value
   * @param executor        the executor
   * @param historySize     the history size
   * @param historyPeriod   the history period
   * @param historyTimeUnit the history time unit
   */
  public SampledStatisticImpl(LatencyImpl latency, ValueStatistic<T> value, ScheduledExecutorService executor, int historySize, long historyPeriod, TimeUnit historyTimeUnit, StatisticType type) {
    super(value, executor, historySize, historyPeriod, historyTimeUnit, type);
    this.latency = latency;
  }

  @Override
  public boolean active() {
    return latency.active();
  }

  @Override
  public T value() {
    latency.touch();
    return super.value();
  }

  @Override
  public List<Timestamped<T>> history() {
    latency.touch();
    return super.history();
  }

  @Override
  public List<Timestamped<T>> history(long since) {
    latency.touch();
    return super.history(since);
  }
}

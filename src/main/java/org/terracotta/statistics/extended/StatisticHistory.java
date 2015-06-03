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
import org.terracotta.statistics.archive.StatisticArchive;
import org.terracotta.statistics.archive.StatisticSampler;
import org.terracotta.statistics.archive.Timestamped;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Class StatisticHistory.
 *
 * @param <T> the generic type
 * @author cdennis
 */
class StatisticHistory<T extends Number> {

  /**
   * The sampler.
   */
  private final StatisticSampler<T> sampler;

  /**
   * The history.
   */
  private final StatisticArchive<T> history;

  /**
   * Instantiates a new sampled statistic.
   *
   * @param statistic      the statistic
   * @param executor       the executor
   * @param historySize    the history size
   * @param period         the period
   * @param periodTimeUnit the period's time unit
   */
  public StatisticHistory(ValueStatistic<T> statistic, ScheduledExecutorService executor, int historySize, long period, TimeUnit periodTimeUnit) {
    this.history = new StatisticArchive<T>(historySize);
    this.sampler = new StatisticSampler<T>(executor, period, periodTimeUnit, statistic, history);
  }

  /**
   * Start sampling.
   */
  public void startSampling() {
    sampler.start();
  }

  /**
   * Stop sampling.
   */
  public void stopSampling() {
    sampler.stop();
    history.clear();
  }

  /**
   * History.
   *
   * @return the list
   */
  public List<Timestamped<T>> history() {
    return history.getArchive();
  }

  /**
   * Adjust.
   *
   * @param historySize     the history size
   * @param historyPeriod   the history period
   * @param historyTimeUnit the history period's time unit
   */
  void adjust(int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    history.setCapacity(historySize);
    sampler.setPeriod(historyPeriod, historyTimeUnit);
  }
}

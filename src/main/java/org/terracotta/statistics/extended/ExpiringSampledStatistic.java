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

import java.io.Serializable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Statistic that stops sampling history when the last access is after a user supplied timestamp.
 *
 * @param <T> statistic type
 * @author Chris Dennis
 */
public class ExpiringSampledStatistic<T extends Serializable> extends SemiExpiringSampledStatistic<T> {

  /**
   * Creates an expiring statistic.
   *
   * @param source          statistic source
   * @param executor        executor to use for sampling
   * @param historySize     size of sample history
   * @param historyPeriod   period between samples
   * @param historyTimeUnit unit of period between samples
   */
  public ExpiringSampledStatistic(ValueStatistic<T> source, ScheduledExecutorService executor, int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    super(source, executor, historySize, historyPeriod, historyTimeUnit);
  }

  @Override
  public T value() {
    touch();
    return super.value();
  }
}

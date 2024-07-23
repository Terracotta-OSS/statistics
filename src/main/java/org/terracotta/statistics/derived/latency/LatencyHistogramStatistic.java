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

import org.terracotta.statistics.ValueStatistic;

import java.util.function.Function;

import static org.terracotta.statistics.ValueStatistics.gauge;

/**
 * @author Mathieu Carbou
 */
public interface LatencyHistogramStatistic extends LatencyHistogramQuery {

  /**
   * Enables to query the histogram several times within a synchronized state so that every calls are consistent
   */
  <T> T query(Function<LatencyHistogramQuery, T> fn);

  default ValueStatistic<Long> percentileStatistic(double percent) {
    return gauge(() -> percentile(percent));
  }

  default ValueStatistic<Long> minimumStatistic() {
    return gauge(this::minimum);
  }

  default ValueStatistic<Long> maximumStatistic() {
    return gauge(this::maximum);
  }

  default ValueStatistic<Long> medianStatistic() {
    return gauge(this::median);
  }

}

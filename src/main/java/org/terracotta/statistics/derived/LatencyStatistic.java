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
package org.terracotta.statistics.derived;

import org.terracotta.statistics.ValueStatistic;

import static org.terracotta.statistics.ValueStatistics.gauge;

/**
 * @author Mathieu Carbou
 */
public interface LatencyStatistic {

  /**
   * @return The minimum in ns or null if it does not exist yet
   */
  Long minimum();

  /**
   * @return The maximum in ns or null if it does not exist yet
   */
  Long maximum();

  /**
   * @return The average in ns or null if it does not exist yet
   */
  Double average();

  default ValueStatistic<Double> averageStatistic() {
    return gauge(this::average);
  }

  default ValueStatistic<Long> minimumStatistic() {
    return gauge(this::minimum);
  }

  default ValueStatistic<Long> maximumStatistic() {
    return gauge(this::maximum);
  }

}

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
package org.terracotta.statistics.derived.latency;

import org.terracotta.statistics.derived.histogram.Histogram;

import java.util.List;

/**
 * @author Mathieu Carbou
 */
public interface LatencyHistogramQuery {

  /**
   * @return The minimum value or null if no value
   */
  Long minimum();

  /**
   * @return The maximum value or null if no value
   */
  Long maximum();

  /**
   * @return the median value or null if no value. Will return the upper bound of the approximated range.
   */
  default Long median() {
    return percentile(.5);
  }

  long count();

  /**
   * @param percent the percentage (0.0-1.0)
   * @return the value below which percent% of the observations may be found. Will return the upper bound of the approximated range.
   */
  Long percentile(double percent);

  /**
   * @param percent the percentage (0.0-1.0)
   * @return the inclusive bounds (min and max) representing the range containing the right value for the given percent%-ile.
   */
  long[] percentileBounds(double percent);

  List<Histogram.Bucket> buckets();

}

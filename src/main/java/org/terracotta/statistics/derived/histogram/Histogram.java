/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company.
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
package org.terracotta.statistics.derived.histogram;

import java.util.List;

import static java.lang.Math.nextDown;

/**
 * A histogram supporting double values
 */
public interface Histogram {

  /**
   * Returns the histograms buckets
   *
   * @return the histogram buckets
   */
  List<Bucket> getBuckets();

  /**
   * The minimum value.
   * <p>
   *   This is equal to the inclusive lower bound of the zeroth (0.0) quantile.
   * </p>
   * @return the minimum value
   */
  default double getMinimum() {
    return getQuantileBounds(0.0)[0];
  }

  /**
   * The maximum value.
   * <p>
   *   This is equal to highest double value strictly less than the exclusive upper bound of the last (1.0) quantile.
   * </p>
   *
   * @return the maximum value
   */
  default double getMaximum() {
    return nextDown(getQuantileBounds(1.0)[1]);
  }

  /**
   * Returns the bounds {@code [minimum, maximum)} on the given quantile.
   *
   * @param quantile desired quantile
   * @return the quantile bounds
   * @throws IllegalArgumentException if {@code quantile} if outside the range [0.0..1.0]
   */
  double[] getQuantileBounds(double quantile) throws IllegalArgumentException;

  /**
   * @return the number of elements in the histogram
   */
  long size();

  /**
   * @return the bounds on the number of elements in the histogram
   */
  double[] getSizeBounds();

  void event(double value, long time);

  void expire(long time);

  /**
   * A histogram bucket.
   */
  interface Bucket {

    /**
     * Returns the bucket minimum (inclusive).
     *
     * @return the bucket minimum
     */
    double minimum();

    /**
     * Returns the bucket maximum (exclusive).
     *
     * @return the bucket maximum
     */
    double maximum();

    /**
     * Returns the count of events in this bucket.
     *
     * @return the bucket event count
     */
    double count();
  }
}

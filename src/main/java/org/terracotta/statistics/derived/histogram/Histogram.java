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
package org.terracotta.statistics.derived.histogram;

import java.util.List;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Stream.of;

/**
 * A histogram.
 *
 * @param <T> x-axis numerical type
 */
public interface Histogram<T extends Number> {

  /**
   * Returns the histograms buckets
   *
   * @return the histogram buckets
   */
  List<Bucket<T>> getBuckets();

  /**
   * Returns the bounds {@code [minimum, maximum]} on the given quantile.
   *
   * @param quantile desired quantile
   * @return the quantile bounds
   * @throws IllegalArgumentException if {@code quantile} if outside the range [0.0..1.0]
   */
  T[] getQuantileBounds(double quantile) throws IllegalArgumentException;

  /**
   * A histogram bucket.
   *
   * @param <T> numerical type of the bucket bounds
   */
  interface Bucket<T extends Number> {

    /**
     * Returns the bucket minimum (inclusive).
     *
     * @return the bucket minimum
     */
    T minimum();

    /**
     * Returns the bucket maximum (exclusive).
     *
     * @return the bucket maximum
     */
    T maximum();

    /**
     * Returns the count of events in this bucket.
     *
     * @return the bucket event count
     */
    double count();
  }
}

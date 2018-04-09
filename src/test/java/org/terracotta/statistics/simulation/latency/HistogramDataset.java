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
package org.terracotta.statistics.simulation.latency;

import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.terracotta.statistics.derived.histogram.Histogram;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * @author Mathieu Carbou
 */
class HistogramDataset extends AbstractIntervalXYDataset {

  private static final long serialVersionUID = 1L;

  private final String key;
  private final List<Histogram.Bucket> buckets;
  private final DomainOrder domainOrder;
  private final DoubleUnaryOperator xAxisTransform;

  HistogramDataset(String serieName, List<Histogram.Bucket> buckets, DomainOrder domainOrder, DoubleUnaryOperator xAxisTransform) {
    this.key = serieName;
    this.buckets = buckets;
    this.domainOrder = domainOrder;
    this.xAxisTransform = xAxisTransform;
  }

  @Override
  public DomainOrder getDomainOrder() {
    return domainOrder;
  }

  @Override
  public int getSeriesCount() {
    return 1;
  }

  @Override
  public Comparable getSeriesKey(int series) {
    ensure0(series);
    return key;

  }

  @Override
  public Number getStartX(int series, int item) {
    ensure0(series);
    return xAxisTransform.applyAsDouble(buckets.get(item).minimum());
  }

  @Override
  public Number getEndX(int series, int item) {
    ensure0(series);
    return xAxisTransform.applyAsDouble(buckets.get(item).maximum());
  }

  @Override
  public Number getStartY(int series, int item) {
    return getY(series, item);
  }

  @Override
  public Number getEndY(int series, int item) {
    return getY(series, item);
  }

  @Override
  public int getItemCount(int series) {
    ensure0(series);
    return buckets.size();
  }

  @Override
  public Number getX(int series, int item) {
    ensure0(series);
    Histogram.Bucket bucket = buckets.get(item);
    return xAxisTransform.applyAsDouble((bucket.minimum() + bucket.maximum()) / 2);
  }

  @Override
  public Number getY(int series, int item) {
    ensure0(series);
    Histogram.Bucket bucket = buckets.get(item);
    return (1.0 / xAxisTransform.applyAsDouble(1.0)) * bucket.count() / (bucket.maximum() - bucket.minimum());
  }

  private void ensure0(int series) {
    if (series != 0) {
      throw new IndexOutOfBoundsException();
    }
  }

}

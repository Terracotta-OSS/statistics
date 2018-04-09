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
import org.jfree.data.xy.AbstractXYDataset;
import org.terracotta.statistics.simulation.latency.LongSample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * @author Mathieu Carbou
 */
public class LongSampleDataset extends AbstractXYDataset {

  private static final long serialVersionUID = 1L;

  private final Map<String, List<LongSample>> series;
  private final List<String> keys;
  private final DomainOrder domainOrder;
  private final DoubleUnaryOperator xAxisTransform;
  private final DoubleUnaryOperator yAxisTransform;

  public LongSampleDataset(List<LongSample> oneSerie, DomainOrder domainOrder, DoubleUnaryOperator xAxisTransform, DoubleUnaryOperator yAxisTransform) {
    this(Collections.singletonMap("", oneSerie), domainOrder, xAxisTransform, yAxisTransform);
  }

  public LongSampleDataset(Map<String, List<LongSample>> series, DomainOrder domainOrder, DoubleUnaryOperator xAxisTransform, DoubleUnaryOperator yAxisTransform) {
    this.keys = new ArrayList<>(series.keySet());
    this.series = series;
    this.domainOrder = domainOrder;
    this.xAxisTransform = xAxisTransform;
    this.yAxisTransform = yAxisTransform;
  }

  @Override
  public int getSeriesCount() {
    return keys.size();
  }

  @Override
  public Comparable getSeriesKey(int series) {
    return keys.get(series);
  }

  @Override
  public int indexOf(Comparable seriesKey) {
    return keys.indexOf(seriesKey);
  }

  @Override
  public DomainOrder getDomainOrder() {
    return domainOrder;
  }

  @Override
  public int getItemCount(int series) {
    String key = keys.get(series);
    return this.series.get(key).size();
  }

  @Override
  public double getXValue(int series, int item) {
    String key = keys.get(series);
    return xAxisTransform.applyAsDouble(this.series.get(key).get(item).time());
  }

  @Override
  public Number getX(int series, int item) {
    return getXValue(series, item);
  }

  @Override
  public double getYValue(int series, int item) {
    String key = keys.get(series);
    return yAxisTransform.applyAsDouble(this.series.get(key).get(item).value());
  }

  @Override
  public Number getY(int series, int item) {
    return getYValue(series, item);
  }

}

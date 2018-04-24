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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.terracotta.statistics.derived.latency.LatencyHistogramStatistic;

import java.text.NumberFormat;
import java.util.function.DoubleUnaryOperator;

/**
 * @author Mathieu Carbou
 */
public class BoundedValue {

  private final double value;
  private final double min;
  private final double max;

  private BoundedValue(double value, double min, double max) {
    this.value = value;
    this.min = min;
    this.max = max;
  }

  public double value() {
    return value;
  }

  public double min() {
    return min;
  }

  public double max() {
    return max;
  }

  public double[] bounds() {
    return new double[]{min, max};
  }

  @Override
  public String toString() {
    return toString(NumberFormat.getNumberInstance());
  }

  public String toString(NumberFormat numberFormat) {
    return min == max ? numberFormat.format(value) : (numberFormat.format(value) + " [" + numberFormat.format(min) + "; " + numberFormat.format(max) + "]");
  }

  public static BoundedValue min(LatencyHistogramStatistic statistic, DoubleUnaryOperator transform) {
    double v = transform.applyAsDouble(statistic.minimum());
    return new BoundedValue(v, v, v);
  }

  public static BoundedValue min(DescriptiveStatistics statistic, DoubleUnaryOperator transform) {
    double v = transform.applyAsDouble(statistic.getMin());
    return new BoundedValue(v, v, v);
  }

  public static BoundedValue max(LatencyHistogramStatistic statistic, DoubleUnaryOperator transform) {
    double v = transform.applyAsDouble(statistic.maximum());
    return new BoundedValue(v, v, v);
  }

  public static BoundedValue max(DescriptiveStatistics statistic, DoubleUnaryOperator transform) {
    double v = transform.applyAsDouble(statistic.getMax());
    return new BoundedValue(v, v, v);
  }

  public static BoundedValue pct(LatencyHistogramStatistic statistic, DoubleUnaryOperator transform, double pct) {
    long v = statistic.percentile(pct);
    long[] bounds = statistic.percentileBounds(pct);
    return new BoundedValue(transform.applyAsDouble(v), transform.applyAsDouble(bounds[0]), transform.applyAsDouble(bounds[1]));
  }

  public static BoundedValue pct(DescriptiveStatistics statistic, DoubleUnaryOperator transform, double pct) {
    double v = transform.applyAsDouble(statistic.getPercentile(pct * 100));
    return new BoundedValue(v, v, v);
  }

  public static BoundedValue median(LatencyHistogramStatistic statistic, DoubleUnaryOperator transform) {
    long v = statistic.median();
    long[] bounds = statistic.percentileBounds(.5);
    return new BoundedValue(transform.applyAsDouble(v), transform.applyAsDouble(bounds[0]), transform.applyAsDouble(bounds[1]));
  }

  public static BoundedValue median(DescriptiveStatistics statistic, DoubleUnaryOperator transform) {
    double v = transform.applyAsDouble(statistic.getPercentile(50));
    return new BoundedValue(v, v, v);
  }

}

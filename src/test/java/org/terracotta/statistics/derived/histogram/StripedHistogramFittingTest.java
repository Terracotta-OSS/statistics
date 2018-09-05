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

import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.terracotta.statistics.derived.histogram.StripedHistogram;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.DoubleStream.generate;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

public class StripedHistogramFittingTest extends HistogramFittingTest {

  public StripedHistogramFittingTest(long seed, double biasRange, int bars, double slopeError, double centroidError, double widthError) {
    super(seed, biasRange, bars, slopeError, centroidError, widthError);
  }

  @Override
  protected Histogram histogram(double bias, int bars, DoubleStream data) {
    StripedHistogram hist = new StripedHistogram(bias, bars, Long.MAX_VALUE);
    data.parallel().forEach(d -> hist.event(d, 0));
    return hist;
  }
}

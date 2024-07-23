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

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.DoubleStream.generate;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public abstract class HistogramFittingTest {

  private static final double ERROR_THRESHOLD = 10;

  private final long seed;
  private final double bias;
  private final int bars;

  private final double slopeError;
  private final double widthError;
  private final double centroidError;

  @Parameterized.Parameters(name = "{index}: seed={0}, bias={1}, bars={2}")
  public static Iterable<Object[]> data() {
    Random rndm = new Random();
    // seed, bias, bars, slope-error, centroid-error, width-error
    return Arrays.asList(new Object[][] {
        {rndm.nextLong(), 0.01, 20, 0.00205, 0.0153, 0.0121},
        {rndm.nextLong(), 0.01, 100, 0.00202, 0.00551, 0.00479},
        {rndm.nextLong(), 0.01, 1000, 0.00248, 0.00509, 0.00497},

        {rndm.nextLong(), 0.1, 20, 0.00137, 0.00710, 0.00746},
        {rndm.nextLong(), 0.1, 100, 0.00130, 0.00511, 0.00510},
        {rndm.nextLong(), 0.1, 1000, 0.00134, 0.00480, 0.00509},

        {rndm.nextLong(), 1, 20, 0.00118, 0.00557, 0.00703},
        {rndm.nextLong(), 1, 100, 0.00110, 0.00508, 0.00532},
        {rndm.nextLong(), 1, 1000, 0.00102, 0.00497, 0.00531},

        {rndm.nextLong(), 10, 20, 0.00134, 0.00712, 0.00797},
        {rndm.nextLong(), 10, 100, 0.00124, 0.00499, 0.00500},
        {rndm.nextLong(), 10, 1000, 0.00131, 0.00499, 0.00512},

        {rndm.nextLong(), 100, 20, 0.00193, 0.0151, 0.0123},
        {rndm.nextLong(), 100, 100, 0.00199, 0.00527, 0.00471},
        {rndm.nextLong(), 100, 1000, 0.00253, 0.00506, 0.00473},
    });
  }

  public HistogramFittingTest(long seed, double biasRange, int bars, double slopeError, double centroidError, double widthError) {
    this.seed = seed;
    this.bias = Math.pow(biasRange, 1.0 / bars);
    this.bars = bars;

    this.slopeError = slopeError;
    this.centroidError = centroidError;
    this.widthError = widthError;
  }

  /*
   * This 'test' is used to calculate the standard-deviations contained in the parameter arrays above.
   * It should never need to be run again, but is left here for posterity.
   */
  @Test
  @Ignore
  public void evaluateFlatHistogramErrors() {
    StandardDeviation parameter = new StandardDeviation();
    for (int i = 0; i < 1000; i++) {
      parameter.increment(flatHistogramFit(System.nanoTime())[1]);
      System.out.println("Flat Histogram Slope: Iteration: " + i + " S.D:" + parameter.getResult());
    }
  }

  @Test
  public void testHistogramOfFlatDistribution() {
    double[] parameters = flatHistogramFit(seed);
    assertThat("slope", parameters[1], closeTo(0.0, slopeError * ERROR_THRESHOLD));
  }

  private double[] flatHistogramFit(long seed) {
    Random rndm = new Random(seed);

    Histogram hist = histogram(bias, bars, rndm.doubles().map(d -> d * 1000.0).limit(100000));
    return fit(hist, PolynomialCurveFitter.create(1));
  }

  /*
   * This 'test' is used to calculate the standard-deviations contained in the parameter arrays above.
   * It should never need to be run again, but is left here for posterity.
   */
  @Test
  @Ignore
  public void evaluateGaussianHistogramErrors() {
    StandardDeviation centroid = new StandardDeviation();
    StandardDeviation width = new StandardDeviation();
    for (int i = 0; i < 1000; i++) {
      double[] parameters = gaussianHistogramFit(System.nanoTime());
      centroid.increment(parameters[1]);
      width.increment(parameters[2]);
      System.out.println("Gaussian Histogram Centroid: Iteration: " + i + " S.D:" + centroid.getResult());
      System.out.println("Gaussian Histogram Width: Iteration: " + i + " S.D:" + width.getResult());
    }
  }

  @Test
  public void testHistogramOfGaussianDistribution() {
    double[] parameters = gaussianHistogramFit(seed);
    assertThat("centroid", parameters[1], closeTo(0, centroidError * ERROR_THRESHOLD));
    assertThat("width", parameters[2], closeTo(1, widthError * ERROR_THRESHOLD));
  }

  private double[] gaussianHistogramFit(long seed) {
    Random rndm = new Random(seed);
    Histogram hist = histogram(bias, bars, generate(rndm::nextGaussian).limit(100000));
    return fit(hist, GaussianCurveFitter.create());
  }

  public static double[] fit(Histogram histogram, AbstractCurveFitter fitter) {
    return histogram.getBuckets().stream()
        .map(b -> new WeightedObservedPoint(1.0, (b.maximum() + b.minimum()) / 2.0, b.count() / (b.maximum() - b.minimum())))
        .collect(collectingAndThen(toList(), fitter::fit));
  }

  protected abstract Histogram histogram(double bias, int bars, DoubleStream data);
}

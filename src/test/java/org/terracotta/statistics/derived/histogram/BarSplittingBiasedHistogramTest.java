/*
 * Copyright 2015 Terracotta, Inc..
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

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.hamcrest.core.IsInstanceOf.any;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class BarSplittingBiasedHistogramTest {

  private static final double ERROR_THRESHOLD = 10;

  private final float bias;
  private final int bars;

  private final double slopeError;
  private final double widthError;
  private final double centroidError;

  @Parameterized.Parameters(name = "{index}: bias={0}, bars={1}")
  public static Iterable<Object[]> data() {
    // bias, bars, slope-error, centroid-error, width-error
    return Arrays.asList(new Object[][] {
            {0.01f, 20, 0.00205, 0.0153, 0.0121},
            {0.01f, 100, 0.00202, 0.00551, 0.00479},
            {0.01f, 1000, 0.00248, 0.00509, 0.00497},

            {0.1f, 20, 0.00137, 0.00710, 0.00746},
            {0.1f, 100, 0.00130, 0.00511, 0.00510},
            {0.1f, 1000, 0.00134, 0.00480, 0.00509},

            {1f, 20, 0.00118, 0.00557, 0.00703},
            {1f, 100, 0.00110, 0.00508, 0.00532},
            {1f, 1000, 0.00102, 0.00497, 0.00531},

            {10f, 20, 0.00134, 0.00712, 0.00797},
            {10f, 100, 0.00124, 0.00499, 0.00500},
            {10f, 1000, 0.00131, 0.00499, 0.00512},

            {100f, 20, 0.00193, 0.0151, 0.0123},
            {100f, 100, 0.00199, 0.00527, 0.00471},
            {100f, 1000, 0.00253, 0.00506, 0.00473},
    });
  }

  public BarSplittingBiasedHistogramTest(float biasRange, int bars, double slopeError, double centroidError, double widthError) {
    this.bias = (float) Math.pow(biasRange, 1.0 / bars);
    this.bars = bars;

    this.slopeError = slopeError;
    this.centroidError = centroidError;
    this.widthError = widthError;
  }

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
    long seed = System.nanoTime();
    try {
      assertThat(asDouble(flatHistogramFit(seed)), arrayContaining(any(Double.class), closeTo(0.0, slopeError * ERROR_THRESHOLD)));
    } catch (Throwable t) {
      throw (AssertionError) new AssertionError("Seed: " + seed).initCause(t);
    }
  }

  private double[] flatHistogramFit(long seed) {
    Random rndm = new Random(seed);

    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(bias, bars, Long.MAX_VALUE);

    double range = 1000;
    for (int i = 0; i < 100000; i++) {
      bsbh.event(rndm.nextDouble() * range, i);
    }

    WeightedObservedPoints points = new WeightedObservedPoints();
    for (Histogram.Bucket<Double> b : bsbh.getBuckets()) {
      points.add((b.maximum() + b.minimum()) / 2.0, b.count() / (b.maximum() - b.minimum()));
    }
    return PolynomialCurveFitter.create(1).fit(points.toList());
  }

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
    long seed = System.nanoTime();
    try {
      assertThat(asDouble(gaussianHistogramFit(seed)), arrayContaining(any(Double.class), closeTo(0, centroidError * ERROR_THRESHOLD), closeTo(1, widthError * ERROR_THRESHOLD)));
    } catch (Throwable t) {
      throw (AssertionError) new AssertionError("Seed: " + seed).initCause(t);
    }
  }

  private double[] gaussianHistogramFit(long seed) {
    Random rndm = new Random(seed);

    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(bias, bars, Long.MAX_VALUE);

    for (int i = 0; i < 100000; i++) {
      bsbh.event(rndm.nextGaussian(), i);

    }

    WeightedObservedPoints points = new WeightedObservedPoints();
    for (Histogram.Bucket<Double> b : bsbh.getBuckets()) {
      points.add((b.maximum() + b.minimum()) / 2.0, b.count() / (b.maximum() - b.minimum()));
    }
    return GaussianCurveFitter.create().fit(points.toList());
  }

  private static Double[] asDouble(double[] input) {
    Double[] output = new Double[input.length];
    for (int i = 0; i < output.length; i++) {
      output[i] = input[i];
    }
    return output;
  }

}

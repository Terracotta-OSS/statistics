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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import static java.lang.Math.ceil;
import static java.util.stream.DoubleStream.generate;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

@RunWith(Parameterized.class)
public class BarSplittingBiasedHistogramPreciseTest {

  @Rule
  public final ErrorCollector errors = new ErrorCollector();

  private static final double ERROR_THRESHOLD = 10;

  private final long seed;
  private final float bias;
  private final int bars;

  @Parameterized.Parameters(name = "{index}: seed={0} bias={1}, bars={2}")
  public static Iterable<Object[]> data() {
    Random rndm = new Random();
    // seed, bias, bars
    return Arrays.asList(new Object[][] {
        {rndm.nextLong(), 0.01f, 20},
        {rndm.nextLong(), 0.01f, 100},
        {rndm.nextLong(), 0.01f, 1000},

        {rndm.nextLong(), 0.1f, 20},
        {rndm.nextLong(), 0.1f, 100},
        {rndm.nextLong(), 0.1f, 1000},

        {rndm.nextLong(), 1f, 20},
        {rndm.nextLong(), 1f, 100},
        {rndm.nextLong(), 1f, 1000},

        {rndm.nextLong(), 10f, 20},
        {rndm.nextLong(), 10f, 100},
        {rndm.nextLong(), 10f, 1000},

        {rndm.nextLong(), 100f, 20},
        {rndm.nextLong(), 100f, 100},
        {rndm.nextLong(), 100f, 1000},
    });
  }

  public BarSplittingBiasedHistogramPreciseTest(long seed, float biasRange, int bars) {
    this.bias = (float) Math.pow(biasRange, 1.0 / bars);
    this.bars = bars;
    this.seed = seed;
  }

  @Test
  public void testHistogramOfFlatDistribution() {
    Random rndm = new Random(seed);

    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(bias, bars, Long.MAX_VALUE);

    checkPercentiles(generate(() -> rndm.nextDouble() * 1000).limit(100000), bsbh, 0.01, 0.1, 0.25, 0.5, 0.75, 0.9, 0.99);
  }

  @Test
  public void testHistogramOfGaussianDistribution() {
    Random rndm = new Random(seed);

    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(bias, bars, Long.MAX_VALUE);

    checkPercentiles(generate(rndm::nextGaussian).limit(100000), bsbh, 0.01, 0.1, 0.25, 0.5, 0.75, 0.9, 0.99);
  }

  private void checkPercentiles(DoubleStream data, BarSplittingBiasedHistogram histogram, double ... quantiles) {
    double[] values = data.toArray();
    for (double d : values) {
      histogram.event(d, 0);
    }

    Arrays.sort(values);

    for (double q : quantiles) {
      double[] bounds = histogram.getQuantileBounds(q);

      double ip = (values.length * q) - 1;
      double ceil = ceil(ip);
      if (ip == ceil) {
        errors.checkThat("Quantile " + q + " lower bound:", bounds[0], lessThanOrEqualTo(values[(int) ip]));
        errors.checkThat("Quantile " + q + " upper bound:", bounds[1], greaterThan(values[(int) (ip + 1)]));
      } else {
        errors.checkThat("Quantile " + q + " lower bound:", bounds[0], lessThanOrEqualTo(values[(int) ceil]));
        errors.checkThat("Quantile " + q + " upper bound:", bounds[1], greaterThan(values[(int) ceil]));
      }
    }
  }
}

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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.DescribedAs;
import org.hamcrest.core.IsNot;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.sort;
import static java.util.Arrays.stream;
import static java.util.stream.DoubleStream.concat;
import static java.util.stream.DoubleStream.generate;
import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public abstract class HistogramPreciseTest {

  private static final double[] HIGH_QUANTILES = new double[] {0.5, 0.75, 0.9, 0.99};
  private static final double[] LOW_QUANTILES = stream(HIGH_QUANTILES).map(d -> 1 - d).toArray();
  private static final double[] ALL_QUANTILES = concat(stream(LOW_QUANTILES), stream(HIGH_QUANTILES)).distinct().toArray();
  private final long seed;
  private final double bias;
  private final int bars;
  private final double[] quantiles;

  @Parameterized.Parameters(name = "{index}: seed={0} bias={1}, bars={2}")
  public static Iterable<Object[]> data() {
    Random rndm = new Random();
    // seed, bias, bars
    return asList(new Object[][] {
        {rndm.nextLong(), 0.01, 20, HIGH_QUANTILES},
        {rndm.nextLong(), 0.01, 100, HIGH_QUANTILES},
        {rndm.nextLong(), 0.01, 1000, HIGH_QUANTILES},

        {rndm.nextLong(), 0.1, 20, HIGH_QUANTILES},
        {rndm.nextLong(), 0.1, 100, HIGH_QUANTILES},
        {rndm.nextLong(), 0.1, 1000, HIGH_QUANTILES},

        {rndm.nextLong(), 1.0, 20, ALL_QUANTILES},
        {rndm.nextLong(), 1.0, 100, ALL_QUANTILES},
        {rndm.nextLong(), 1.0, 1000, ALL_QUANTILES},

        {rndm.nextLong(), 10.0, 20, LOW_QUANTILES},
        {rndm.nextLong(), 10.0, 100, LOW_QUANTILES},
        {rndm.nextLong(), 10.0, 1000, LOW_QUANTILES},

        {rndm.nextLong(), 100.0, 20, LOW_QUANTILES},
        {rndm.nextLong(), 100.0, 100, LOW_QUANTILES},
        {rndm.nextLong(), 100.0, 1000, LOW_QUANTILES},
    });
  }

  public HistogramPreciseTest(long seed, double biasRange, int bars, double[] quantiles) {
    this.bias = Math.pow(biasRange, 1.0 / bars);
    this.bars = bars;
    this.seed = seed;
    this.quantiles = quantiles;
  }

  @Test
  public void testHistogramOfFlatDistribution() {
    Random rndm = new Random(seed);

    Histogram bsbh = histogram(bias, bars, Long.MAX_VALUE);

    double slope = (rndm.nextDouble() * 999.99) + 0.01;
    double offset = (rndm.nextDouble() - 0.5) * 1000;
    checkHistogram(generate(rndm::nextDouble).map(x -> (x * slope) + offset).limit(100000), bsbh, quantiles);
  }

  @Test
  public void testHistogramOfGaussianDistribution() {
    Random rndm = new Random(seed);

    Histogram bsbh = histogram(bias, bars, Long.MAX_VALUE);

    double width = (rndm.nextDouble() * 999.99) + 0.01;
    double centroid = (rndm.nextDouble() - 0.5) * 1000;

    checkHistogram(generate(rndm::nextGaussian).map(x -> (x * width) + centroid).limit(100000), bsbh, quantiles);
  }

  private void checkHistogram(DoubleStream data, Histogram histogram, double ... quantiles) {
    double[] values = data.toArray();

    feedHistogram(histogram, values);

    sort(values);

    assertThat(histogram.getMinimum(), is(values[0]));
    assertThat(histogram.getMaximum(), is(values[values.length - 1]));
    assertThat(histogram.getSizeBounds(), compatibleWith(values.length));
    for (double q : quantiles) {
      double[] bounds = histogram.getQuantileBounds(q);

      double ip = (values.length * q) - 1;
      double ceil = ceil(ip);
      if (ip == ceil) {
        double lower = values[(int) ip];
        double upper = values[(int) (ip + 1)];
        assertThat("Quantile " + q, bounds, compatibleWith(lower, upper));
      } else {
        double value = values[(int) ceil];
        assertThat("Quantile " + q, bounds, compatibleWith(value));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Matcher<double[]> compatibleWith(double value) {
    return new DescribedAs<double[]>("a range encompassing %0",
        convertedFrom(double[].class, a -> stream(a).boxed().toArray(Double[]::new),
            arrayContaining(lessThanOrEqualTo(value), greaterThan(value))),
        new Object[] {value}) {
      @Override
      public void describeMismatch(Object item, Description description) {
        if (item instanceof double[] && ((double[]) item).length == 2) {
          double[] array = (double[]) item;
          description.appendText("in range [").appendValue(array[0]).appendText(", ").appendValue(array[1]).appendText(") ");
        }
        super.describeMismatch(item, description);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static Matcher<double[]> compatibleWith(double lower, double upper) {
    return new DescribedAs<double[]>("a range fully compatible with [%0, %1]",
        convertedFrom(double[].class, a -> stream(a).boxed().toArray(Double[]::new),
            arrayContaining(lessThanOrEqualTo(upper), greaterThan(lower))),
        new Object[] {lower, upper}) {
      @Override
      public void describeMismatch(Object item, Description description) {
        if (item instanceof double[] && ((double[]) item).length == 2) {
          double[] array = (double[]) item;
          description.appendText("in range [").appendValue(array[0]).appendText(", ").appendValue(array[1]).appendText(") ");
        }
        super.describeMismatch(item, description);
      }
    };
  }

  @Test
  public void testFlipFlop() throws IOException {
    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(bias, bars, 100000);
    Random rndm = new Random(seed);

    long time = 0;

    for (int c = 0; c < 10; c++) {
      long centroid = rndm.nextInt(3000) - 1500;
      long width = rndm.nextInt(3000) + 100;

      for (double datum : rndm.doubles(100000).map(d -> (d * width) + centroid).toArray()) {
        bsbh.event(datum, time++);
      }
    }
  }

  @Test
  public void testSplitBiasEffectOnMaximum() {
    assumeThat("Random bias for equi-depth - skipping", bias, not(1.0));

    Random rndm = new Random(seed);

    Histogram hist = histogram(bias, bars, 1000);

    double width = (rndm.nextDouble() * 999.99) + 0.01;
    double centroid = (rndm.nextDouble() - 0.5) * 1000;

    double[] values = generate(rndm::nextGaussian).map(x -> (x * width) + centroid).limit(10000).toArray();
    double[] window = new double[0];
    for (int i = 0; i < values.length; i++) {
      hist.event(values[i], i);
      hist.expire(i);

      window = copyOfRange(window, max(0, window.length - 999), min(1000, window.length + 1));
      window[window.length - 1] = values[i];
      sort(window);

      assertThat(hist.getSizeBounds(), compatibleWith(window.length));
      if (bias < 1.0) {
        assertThat(hist.getMaximum(), greaterThanOrEqualTo(window[window.length - 1]));
      } else if (bias > 1.0) {
        assertThat(hist.getMinimum(), lessThanOrEqualTo(window[0]));
      }
    }
  }

  protected abstract Histogram histogram(double bias, int bars, long window);

  protected abstract void feedHistogram(Histogram histogram, double[] values);


  private static <T, U> Matcher<T> convertedFrom(Class<T> type, Function<T, U> mapper, Matcher<U> matcher) {
    return new TypeSafeMatcher<T>(type) {
      @Override
      public void describeTo(Description description) {
        matcher.describeTo(description);
      }

      @Override
      protected void describeMismatchSafely(T item, Description mismatchDescription) {
        matcher.describeMismatch(mapper.apply(item), mismatchDescription);
      }

      @Override
      protected boolean matchesSafely(T item) {
        return matcher.matches(mapper.apply(item));
      }
    };
  }
}

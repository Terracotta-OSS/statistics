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

import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;
import org.terracotta.statistics.derived.histogram.BarSplittingBiasedHistogram;
import org.terracotta.statistics.derived.histogram.StripedHistogram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.nextUp;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.terracotta.statistics.derived.histogram.StripedHistogram.mergeBars;

public class StripedHistogramTest {

  @Test
  public void testQuantileBoundsOfEmptyHistogram() {
    StripedHistogram hist = new StripedHistogram(10, 100);

    assertArrayEquals(hist.getQuantileBounds(0.0), new double[]{Double.NaN, Double.NaN}, 0.0);
    assertArrayEquals(hist.getQuantileBounds(1.0), new double[]{Double.NaN, Double.NaN}, 0.0);
  }

  @Test
  public void testExpiryClearsFully() {
    StripedHistogram hist = new StripedHistogram(10, 100);
    range(0, 100).parallel().forEach(i -> hist.event(i, i));

    assertThat(hist.getQuantileBounds(0.0)[0], is(0.0));
    assertThat(hist.getQuantileBounds(1.0)[1], is(nextUp(99.0)));

    hist.expire(200);

    assertThat(hist.getQuantileBounds(0.0)[0], is(Double.NaN));
    assertThat(hist.getQuantileBounds(1.0)[1], is(Double.NaN));
  }

  @Test
  public void mergeOfContinuousBarsIsNoOp() {
    List<double[]> bars = range(0, 9).mapToObj(i -> new double[]{i, i + 1, i}).collect(toList());

    assertThat(merge(bars), is(bars));

  }

  @Test
  public void mergeOfDuplicateBarsDoublesCounts() {
    List<double[]> bars = range(0, 9).flatMap(i -> IntStream.of(i, i)).mapToObj(i -> new double[]{i, i + 1, i}).collect(toList());

    assertThat(merge(bars), hasItems(range(0, 9).mapToObj(i -> new double[]{i, i + 1, 2 * i}).toArray(double[][]::new)));
  }

  @Test
  public void mergeOfIntersectionsDoublesCountInIntersection() {
    List<double[]> bars = range(0, 9).mapToObj(i -> new double[]{i, i + 2, i}).collect(toList());

    assertThat(merge(bars), hasItems(
        range(0, 9).mapToObj(i -> {
          switch (i) {
            case 0:
              return new double[]{0, 1, 0.0};
            case 9:
              return new double[]{9, 10, 4.0};
            default:
              return new double[]{i, i + 1, i - 0.5};
          }
        }).toArray(double[][]::new)));
  }

  @Test
  public void randomWorkloadTest() {
    long seed = System.nanoTime();
    Random rndm = new Random(seed);
    try {
      List<double[]> bars = range(0, 100).mapToObj(i -> {
        double start = rndm.nextDouble();
        return new double[]{start, start + rndm.nextDouble(), rndm.nextInt(1000)};
      }).sorted(Comparator.comparingDouble(a -> a[0])).collect(toList());

      merge(bars);
    } catch (Throwable t) {
      throw new AssertionError("Failure with seed " + seed, t);
    }
  }

  private static List<double[]> merge(List<double[]> bars) {
    List<double[]> merged = new LinkedList<>(bars);
    mergeBars(merged);

    assertThat(merged.stream().mapToDouble(a -> a[0]).min(), is(bars.stream().mapToDouble(a -> a[0]).min()));
    assertThat(merged.stream().mapToDouble(a -> a[1]).max(), is(bars.stream().mapToDouble(a -> a[1]).max()));
    assertThat(merged.stream().mapToDouble(a -> a[2]).sum(), is(bars.stream().mapToDouble(a -> a[2]).sum()));

    for (int i = 1; i < merged.size(); i++) {
      assertThat(merged.get(i - 1)[1], lessThanOrEqualTo(merged.get(i)[0]));
    }

    return merged;
  }
}

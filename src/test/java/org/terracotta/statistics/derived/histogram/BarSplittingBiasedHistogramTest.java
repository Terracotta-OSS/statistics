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

import org.junit.Test;

import static java.lang.Math.nextUp;
import static java.util.stream.IntStream.range;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class BarSplittingBiasedHistogramTest {

  @Test
  public void testQuantileBoundsOfEmptyHistogram() {
    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(10, 100);

    assertArrayEquals(bsbh.getQuantileBounds(0.0), new double[]{Double.NaN, Double.NaN}, 0.0);
    assertArrayEquals(bsbh.getQuantileBounds(1.0), new double[]{Double.NaN, Double.NaN}, 0.0);
  }

  @Test
  public void testExpiryClearsFully() {
    BarSplittingBiasedHistogram bsbh = new BarSplittingBiasedHistogram(10, 100);
    range(0, 100).forEach(i -> bsbh.event(i, i));

    assertThat(bsbh.getQuantileBounds(0.0)[0], is(0.0));
    assertThat(bsbh.getQuantileBounds(1.0)[1], is(nextUp(99.0)));

    bsbh.expire(200);

    assertThat(bsbh.getQuantileBounds(0.0)[0], is(Double.NaN));
    assertThat(bsbh.getQuantileBounds(1.0)[1], is(Double.NaN));
  }
}

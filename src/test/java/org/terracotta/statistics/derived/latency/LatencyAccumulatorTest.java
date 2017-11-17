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
package org.terracotta.statistics.derived.latency;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author cdennis
 */
public class LatencyAccumulatorTest {

  @Test
  public void testMinimumBehavior() {
    LatencyAccumulator stats = LatencyAccumulator.empty();

    assertThat(stats.minimum(), nullValue());
    stats.accumulate(100L);
    assertThat(stats.minimum(), is(100L));
    stats.event(0, 200L);
    assertThat(stats.minimum(), is(100L));
    stats.event(0, 99L);
    assertThat(stats.minimum(), is(99L));
    stats.event(0, 0L);
    assertThat(stats.minimum(), is(0L));
    stats.event(0, -1L);
    assertThat(stats.minimum(), is(-1L));
    stats.event(0, 1L);
    assertThat(stats.minimum(), is(-1L));
    stats.event(0, Long.MIN_VALUE);
    assertThat(stats.minimum(), is(Long.MIN_VALUE));
  }

  @Test
  public void testMaximumBehavior() {
    LatencyAccumulator stats = LatencyAccumulator.empty();

    assertThat(stats.maximum(), nullValue());
    stats.event(0, -100L);
    assertThat(stats.maximum(), is(-100L));
    stats.event(0, -200L);
    assertThat(stats.maximum(), is(-100L));
    stats.event(0, -99L);
    assertThat(stats.maximum(), is(-99L));
    stats.event(0, 0L);
    assertThat(stats.maximum(), is(0L));
    stats.event(0, 1L);
    assertThat(stats.maximum(), is(1L));
    stats.event(0, -1L);
    assertThat(stats.maximum(), is(1L));
    stats.event(0, Long.MAX_VALUE);
    assertThat(stats.maximum(), is(Long.MAX_VALUE));
  }

  @Test
  public void testAverageBehavior() {
    LatencyAccumulator stats = LatencyAccumulator.empty();

    assertThat(stats.average(), is(Double.NaN));
    stats.event(0, 1L);
    assertThat(stats.average(), is(1.0));
    stats.event(0, 3L);
    assertThat(stats.average(), is(2.0));
    stats.event(0, 0L);
    stats.event(0, 0L);
    assertThat(stats.average(), is(1.0));
  }

  @Test
  public void testAccumulate() {
    LatencyAccumulator a3 = LatencyAccumulator.empty();
    a3.accumulate(LatencyAccumulator.accumulator(1, 2, 3));
    a3.accumulate(LatencyAccumulator.accumulator(4, 5, 6));

    assertThat(a3.count(), is(6L));
    assertThat(a3.average(), is(3.5D));
    assertThat(a3.minimum(), is(1L));
    assertThat(a3.maximum(), is(6L));
  }
}

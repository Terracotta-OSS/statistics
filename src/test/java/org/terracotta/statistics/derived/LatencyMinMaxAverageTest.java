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
package org.terracotta.statistics.derived;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author cdennis
 */
public class LatencyMinMaxAverageTest {

  @Test
  public void testMinimumBehavior() {
    LatencyMinMaxAverage stats = new LatencyMinMaxAverage();

    assertThat(stats.minimum(), nullValue());
    stats.event(0, 100L);
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
    LatencyMinMaxAverage stats = new LatencyMinMaxAverage();

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
    LatencyMinMaxAverage stats = new LatencyMinMaxAverage();

    assertThat(stats.average(), nullValue());
    stats.event(0, 1L);
    assertThat(stats.average(), is(1.0));
    stats.event(0, 3L);
    assertThat(stats.average(), is(2.0));
    stats.event(0, 0L);
    stats.event(0, 0L);
    assertThat(stats.average(), is(1.0));
  }
}

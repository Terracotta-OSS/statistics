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
package org.terracotta.statistics.derived;

import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author cdennis
 */
public class MinMaxAverageTest {
  
  @Test
  public void testMinimumBehavior() {
    MinMaxAverage stats = new MinMaxAverage();
    
    assertThat(stats.min(), nullValue());
    stats.event(0, 100L);
    assertThat(stats.min(), is(100L));
    stats.event(0, 200L);
    assertThat(stats.min(), is(100L));
    stats.event(0, 99L);
    assertThat(stats.min(), is(99L));
    stats.event(0, 0L);
    assertThat(stats.min(), is(0L));
    stats.event(0, -1L);
    assertThat(stats.min(), is(-1L));
    stats.event(0, 1L);
    assertThat(stats.min(), is(-1L));
    stats.event(0, Long.MIN_VALUE);
    assertThat(stats.min(), is(Long.MIN_VALUE));
  }

  @Test
  public void testMaximumBehavior() {
    MinMaxAverage stats = new MinMaxAverage();
    
    assertThat(stats.max(), nullValue());
    stats.event(0, -100L);
    assertThat(stats.max(), is(-100L));
    stats.event(0, -200L);
    assertThat(stats.max(), is(-100L));
    stats.event(0, -99L);
    assertThat(stats.max(), is(-99L));
    stats.event(0, 0L);
    assertThat(stats.max(), is(0L));
    stats.event(0, 1L);
    assertThat(stats.max(), is(1L));
    stats.event(0, -1L);
    assertThat(stats.max(), is(1L));
    stats.event(0, Long.MAX_VALUE);
    assertThat(stats.max(), is(Long.MAX_VALUE));
  }

  @Test
  public void testAverageBehavior() {
    MinMaxAverage stats = new MinMaxAverage();

    assertThat(stats.mean(), nullValue());
    stats.event(0, 1L);
    assertThat(stats.mean(), is(1.0));
    stats.event(0, 3L);
    assertThat(stats.mean(), is(2.0));
    stats.event(0, 0L);
    stats.event(0, 0L);
    assertThat(stats.mean(), is(1.0));
  }
}

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
package org.terracotta.statistics.derived.latency;

import org.junit.AfterClass;
import org.junit.Test;
import org.terracotta.statistics.MutableTimeSource;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.TimeMocking;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class LatencySimpleMovingAverageTest {

  public static final MutableTimeSource SOURCE = TimeMocking.push(new MutableTimeSource());

  @AfterClass
  public static void installTimeSource() {
    TimeMocking.pop();
  }

  @Test
  public void testNoEventsAverage() {
    assertThat(new LatencySimpleMovingAverage(1, TimeUnit.SECONDS).average(), is(Double.NaN));
    assertThat(new LatencySimpleMovingAverage(1, TimeUnit.SECONDS).minimum(), nullValue());
    assertThat(new LatencySimpleMovingAverage(1, TimeUnit.SECONDS).maximum(), nullValue());
  }

  @Test
  public void testSingleEventAverage() {
    LatencySimpleMovingAverage average = new LatencySimpleMovingAverage(1, TimeUnit.DAYS);
    average.event(Time.time(), 1L);
    assertThat(average.average(), is(1.0));
    assertThat(average.minimum(), is(1L));
    assertThat(average.maximum(), is(1L));
  }

  @Test
  public void testExpiredEventAverage() {
    LatencySimpleMovingAverage average = new LatencySimpleMovingAverage(100, TimeUnit.MILLISECONDS);
    average.event(Time.time(), 1L);
    SOURCE.advanceTime(300, TimeUnit.MILLISECONDS);
    assertThat(average.average(), is(Double.NaN));
    assertThat(average.minimum(), nullValue());
    assertThat(average.maximum(), nullValue());
  }

  @Test
  public void testDoubleEventAverage() {
    LatencySimpleMovingAverage average = new LatencySimpleMovingAverage(1, TimeUnit.DAYS);
    average.event(Time.time(), 1L);
    average.event(Time.time(), 3L);
    assertThat(average.average(), is(2.0));
    assertThat(average.minimum(), is(1L));
    assertThat(average.maximum(), is(3L));
  }

  @Test
  public void testAverageMoves() {
    LatencySimpleMovingAverage average = new LatencySimpleMovingAverage(100, TimeUnit.MILLISECONDS);
    average.event(Time.time(), 1L);
    SOURCE.advanceTime(50, TimeUnit.MILLISECONDS);
    assertThat(average.average(), is(1.0));
    assertThat(average.minimum(), is(1L));
    assertThat(average.maximum(), is(1L));
    average.event(Time.time(), 3L);
    assertThat(average.average(), is(2.0));
    assertThat(average.minimum(), is(1L));
    assertThat(average.maximum(), is(3L));
    SOURCE.advanceTime(75, TimeUnit.MILLISECONDS);
    assertThat(average.average(), is(3.0));
    assertThat(average.minimum(), is(3L));
    assertThat(average.maximum(), is(3L));
    SOURCE.advanceTime(50, TimeUnit.MILLISECONDS);
    assertThat(average.average(), is(Double.NaN));
    assertThat(average.minimum(), nullValue());
    assertThat(average.maximum(), nullValue());
  }
}

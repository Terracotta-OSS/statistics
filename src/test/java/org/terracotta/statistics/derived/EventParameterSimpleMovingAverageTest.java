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

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Test;
import org.terracotta.statistics.MutableTimeSource;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.TimeMocking;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class EventParameterSimpleMovingAverageTest {
  
  public static final MutableTimeSource SOURCE = TimeMocking.push(new MutableTimeSource());
  
  @AfterClass
  public static void installTimeSource() {
    TimeMocking.pop();
  }
  
  @Test
  public void testNoEventsAverage() {
    assertThat(new EventParameterSimpleMovingAverage(1, TimeUnit.SECONDS).average(), is(Double.NaN));
    assertThat(new EventParameterSimpleMovingAverage(1, TimeUnit.SECONDS).minimum(), is(Long.MIN_VALUE));
    assertThat(new EventParameterSimpleMovingAverage(1, TimeUnit.SECONDS).maximum(), is(Long.MAX_VALUE));
  }
  
  @Test
  public void testSingleEventAverage() {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(1, TimeUnit.DAYS);
    average.event(Time.time(), 1L);
    assertThat(average.average(), is(1.0));
    assertThat(average.minimum(), is(1L));
    assertThat(average.maximum(), is(1L));
  }

  @Test
  public void testExpiredEventAverage() throws InterruptedException {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(100, TimeUnit.MILLISECONDS);
    average.event(Time.time(), 1L);
    SOURCE.advanceTime(300, TimeUnit.MILLISECONDS);
    assertThat(average.average(), is(Double.NaN));
    assertThat(average.minimum(), is(Long.MIN_VALUE));
    assertThat(average.maximum(), is(Long.MAX_VALUE));
  }

  @Test
  public void testDoubleEventAverage() {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(1, TimeUnit.DAYS);
    average.event(Time.time(), 1L);
    average.event(Time.time(), 3L);
    assertThat(average.average(), is(2.0));
    assertThat(average.minimum(), is(1L));
    assertThat(average.maximum(), is(3L));
  }
  
  @Test
  public void testAverageMoves() throws InterruptedException {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(100, TimeUnit.MILLISECONDS);
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
    assertThat(average.minimum(), is(Long.MIN_VALUE));
    assertThat(average.maximum(), is(Long.MAX_VALUE));
  }
}

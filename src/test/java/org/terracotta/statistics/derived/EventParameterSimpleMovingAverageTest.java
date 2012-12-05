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

import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;

public class EventParameterSimpleMovingAverageTest {
  
  @Test
  public void testNoEventsAverage() {
    assertThat(new EventParameterSimpleMovingAverage(1, TimeUnit.SECONDS).average(), is(Double.NaN));
  }
  
  @Test
  public void testSingleEventAverage() {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(1, TimeUnit.DAYS);
    average.event(1L);
    assertThat(average.average(), is(1.0));
  }

  @Test
  public void testExpiredEventAverage() throws InterruptedException {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(100, TimeUnit.MILLISECONDS);
    average.event(1L);
    TimeUnit.MILLISECONDS.sleep(300);
    assertThat(average.average(), is(Double.NaN));
  }

  @Test
  public void testDoubleEventAverage() {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(1, TimeUnit.DAYS);
    average.event(1L);
    average.event(3L);
    assertThat(average.average(), is(2.0));
  }
  
  @Test
  public void testAverageMoves() throws InterruptedException {
    EventParameterSimpleMovingAverage average = new EventParameterSimpleMovingAverage(100, TimeUnit.MILLISECONDS);
    average.event(1L);
    TimeUnit.MILLISECONDS.sleep(50);
    assertThat(average.average(), is(1.0));
    average.event(3L);
    assertThat(average.average(), is(2.0));
    TimeUnit.MILLISECONDS.sleep(75);
    assertThat(average.average(), is(3.0));
    TimeUnit.MILLISECONDS.sleep(50);
    assertThat(average.average(), is(Double.NaN));
  }
}

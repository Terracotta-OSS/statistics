/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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

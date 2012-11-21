/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    stats.event(100L);
    assertThat(stats.min(), is(100L));
    stats.event(200L);
    assertThat(stats.min(), is(100L));
    stats.event(99L);
    assertThat(stats.min(), is(99L));
    stats.event(0L);
    assertThat(stats.min(), is(0L));
    stats.event(-1L);
    assertThat(stats.min(), is(-1L));
    stats.event(1L);
    assertThat(stats.min(), is(-1L));
    stats.event(Long.MIN_VALUE);
    assertThat(stats.min(), is(Long.MIN_VALUE));
  }

  @Test
  public void testMaximumBehavior() {
    MinMaxAverage stats = new MinMaxAverage();
    
    assertThat(stats.max(), nullValue());
    stats.event(-100L);
    assertThat(stats.max(), is(-100L));
    stats.event(-200L);
    assertThat(stats.max(), is(-100L));
    stats.event(-99L);
    assertThat(stats.max(), is(-99L));
    stats.event(0L);
    assertThat(stats.max(), is(0L));
    stats.event(1L);
    assertThat(stats.max(), is(1L));
    stats.event(-1L);
    assertThat(stats.max(), is(1L));
    stats.event(Long.MAX_VALUE);
    assertThat(stats.max(), is(Long.MAX_VALUE));
  }

  @Test
  public void testAverageBehavior() {
    MinMaxAverage stats = new MinMaxAverage();

    assertThat(stats.mean(), nullValue());
    stats.event(1L);
    assertThat(stats.mean(), is(1.0f));
    stats.event(3L);
    assertThat(stats.mean(), is(2.0f));
    stats.event(0L);
    stats.event(0L);
    assertThat(stats.mean(), is(1.0f));
  }
}

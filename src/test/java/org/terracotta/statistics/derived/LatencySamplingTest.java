/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.derived;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.terracotta.statistics.observer.EventObserver;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 * @author cdennis
 */
public class LatencySamplingTest {
  
  @Test
  public void testRateOfZeroNeverSamples() {
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(FooBar.FOO, 0.0f);
    latency.addDerivedStatistic(new EventObserver() {

      @Override
      public void event(long ... parameters) {
        fail();
      }
    });
    
    for (int i = 0; i < 100; i++) {
      latency.begin();
      latency.end(FooBar.FOO);
    }
  }

  @Test
  public void testRateOfOneAlwaysSamples() {
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(FooBar.FOO, 1.0f);
    final AtomicInteger eventCount = new AtomicInteger();
    latency.addDerivedStatistic(new EventObserver() {

      @Override
      public void event(long ... parameters) {
        eventCount.incrementAndGet();
      }
    });
    
    for (int i = 0; i < 100; i++) {
      latency.begin();
      latency.end(FooBar.FOO);
    }
    
    assertThat(eventCount.get(), is(100));
  }

  @Test
  public void testMismatchedResultNeverSamples() {
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(FooBar.FOO, 1.0f);
    latency.addDerivedStatistic(new EventObserver() {

      @Override
      public void event(long ... parameters) {
        fail();
      }
   });
    
    for (int i = 0; i < 100; i++) {
      latency.begin();
      latency.end(FooBar.BAR);
    }
  }
  
  @Test
  public void testLatencyMeasuredAccurately() throws InterruptedException {
    Random random = new Random();
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(FooBar.FOO, 1.0f);
    final AtomicLong expected = new AtomicLong();
    latency.addDerivedStatistic(new EventObserver() {

      @Override
      public void event(long ... parameters) {
        assertThat(parameters[0], greaterThanOrEqualTo(expected.get()));
      }
    });
    
    for (int i = 0; i < 10; i++) {
      latency.begin();
      long start = System.nanoTime();
      TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
      expected.set(System.nanoTime() - start);
      latency.end(FooBar.FOO);
    }
  }
  
  static enum FooBar {
    FOO, BAR;
  }
}

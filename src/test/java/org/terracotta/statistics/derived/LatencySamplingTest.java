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

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.observer.ChainedEventObserver;

import static java.util.EnumSet.*;
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
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(of(FooBar.FOO), 0.0f);
    latency.addDerivedStatistic(new ChainedEventObserver() {

      @Override
      public void event(long time, long ... parameters) {
        fail();
      }
    });
    
    for (int i = 0; i < 100; i++) {
      latency.begin(0);
      latency.end(1, FooBar.FOO);
    }
  }

  @Test
  public void testRateOfOneAlwaysSamples() {
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(of(FooBar.FOO), 1.0f);
    final AtomicInteger eventCount = new AtomicInteger();
    latency.addDerivedStatistic(new ChainedEventObserver() {

      @Override
      public void event(long time, long ... parameters) {
        eventCount.incrementAndGet();
      }
    });
    
    for (int i = 0; i < 100; i++) {
      latency.begin(0);
      latency.end(1, FooBar.FOO);
    }
    
    assertThat(eventCount.get(), is(100));
  }

  @Test
  public void testMismatchedResultNeverSamples() {
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(of(FooBar.FOO), 1.0f);
    latency.addDerivedStatistic(new ChainedEventObserver() {

      @Override
      public void event(long time, long ... parameters) {
        fail();
      }
   });
    
    for (int i = 0; i < 100; i++) {
      latency.begin(0);
      latency.end(1, FooBar.BAR);
    }
  }
  
  @Test
  public void testLatencyMeasuredAccurately() throws InterruptedException {
    Random random = new Random();
    LatencySampling<FooBar> latency = new LatencySampling<FooBar>(of(FooBar.FOO), 1.0f);
    final AtomicLong expected = new AtomicLong();
    latency.addDerivedStatistic(new ChainedEventObserver() {

      @Override
      public void event(long time, long ... parameters) {
        assertThat(parameters[0], greaterThanOrEqualTo(expected.get()));
      }
    });
    
    for (int i = 0; i < 10; i++) {
      long start = random.nextLong();
      int sleep = random.nextInt(100);
      latency.begin(start);
      latency.end(start + sleep, FooBar.FOO);
    }
  }
  
  static enum FooBar {
    FOO, BAR;
  }
}

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

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.CombinableMatcher;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.terracotta.statistics.MutableTimeSource;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.TimeMocking;
import org.terracotta.statistics.observer.ChainedEventObserver;

import static org.hamcrest.number.IsCloseTo.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author cdennis
 */
public class EventRateSimpleMovingAverageTest {
  
  private static final double EXPECTED_ACCURACY = 0.1;
  
  public static final MutableTimeSource SOURCE = TimeMocking.push(new MutableTimeSource());
  
  @AfterClass
  public static void installTimeSource() {
    TimeMocking.pop();
  }
  
  @Test
  public void testNoEventBehavior() {
    EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(1, TimeUnit.HOURS);
    Assert.assertThat(stat.rateUsingSeconds(), Is.is(0.0));
  }

  @Test
  public void testConsistentRate() throws InterruptedException {
    for (int rate = 1; rate < 10; rate++) {
      EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(100, TimeUnit.MILLISECONDS);
      double actualRate = new EventDriver(stat, 10, rate, 20, TimeUnit.MILLISECONDS).call();
      assertThat(stat.rate(TimeUnit.SECONDS), closeTo(actualRate, EXPECTED_ACCURACY * actualRate));
    }
  }
  
  @Test
  public void testChangingRateWithShortPeriodReaches() throws InterruptedException {
    EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(200, TimeUnit.MILLISECONDS);    
    
    double firstRate = new EventDriver(stat, 10, 10, 20, TimeUnit.MILLISECONDS).call();
    assertThat(stat.rate(TimeUnit.SECONDS), closeTo(firstRate, EXPECTED_ACCURACY * firstRate));
    
    double finalRate = new EventDriver(stat, 10, 20, 20, TimeUnit.MILLISECONDS).call();
    assertThat(stat.rate(TimeUnit.SECONDS), closeTo(finalRate, EXPECTED_ACCURACY * finalRate));
  }
  
  @Test
  public void testChangingRateWithLongPeriodDoesntReach() throws InterruptedException {
    EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(60, TimeUnit.SECONDS);    
    
    double firstRate = new EventDriver(stat, 5000, 10, 20, TimeUnit.MILLISECONDS).call();
    double lowRate = stat.rate(TimeUnit.SECONDS);
    assertThat(lowRate, closeTo(firstRate, EXPECTED_ACCURACY * firstRate));
    
    double finalRate = new EventDriver(stat, 10, 20, 20, TimeUnit.MILLISECONDS).call();
    double rate = stat.rate(TimeUnit.SECONDS);
    assertThat(rate, CombinableMatcher.<Double>both(greaterThan(lowRate)).and(lessThan(finalRate)));
  }

  @Test
  public void testContinuousRateSplitAcrossTwoThreads() throws InterruptedException, ExecutionException {
    EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(1, TimeUnit.SECONDS);
    Callable<Double> c1 = new EventDriver(stat, 50, 20, 20, TimeUnit.MILLISECONDS, true);
    Callable<Double> c2 = new EventDriver(stat, 50, 20, 20, TimeUnit.MILLISECONDS, false);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      double totalRate = 0.0;
      for (Future<Double> f : executor.invokeAll(Arrays.<Callable<Double>>asList(c1, c2))) {
        totalRate += f.get().doubleValue();
      }
      assertThat(stat.rate(TimeUnit.SECONDS), closeTo(totalRate, EXPECTED_ACCURACY * totalRate));
    } finally {
      executor.shutdown();
    }
  }

  @Test
  public void testWindowThresholdEffects() throws InterruptedException {
    EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(1, TimeUnit.SECONDS);
    for (long cycles = 0; cycles < 3; cycles++) {
        SOURCE.advanceTime(100, TimeUnit.MILLISECONDS);
        stat.event(Time.time());
        for (long after = 0; after <= 1000; after += 1) {
          assertThat(stat.rateUsingSeconds(), lessThanOrEqualTo(1.0));
          SOURCE.advanceTime(1, TimeUnit.MILLISECONDS);
      }
    }
  }

  static class EventDriver implements Callable<Double> {

    private final ChainedEventObserver stat;
    private final int batches;
    private final int batchSize;
    private final long sleep;
    private final boolean advanceTime;
    
    EventDriver(ChainedEventObserver stat, int events, long period, TimeUnit unit) {
      this(stat, events, 1, period, unit);
    }
    
    EventDriver(ChainedEventObserver stat, int batches, int batchSize, long period, TimeUnit unit) {
      this(stat, batches, batchSize, period, unit, true);
    }
    
    EventDriver(ChainedEventObserver stat, int batches, int batchSize, long period, TimeUnit unit, boolean advanceTime) {
      this.stat = stat;
      this.batches = batches;
      this.batchSize = batchSize;
      this.sleep = unit.toNanos(period);
      this.advanceTime = advanceTime;
    }
    
    @Override
    public Double call() {
      for (int i = 0; i < batches; i++) {
        for (int j = 0; j < batchSize; j++) {
          stat.event(Time.time(), 0L);
        }
        if (advanceTime) {
          SOURCE.advanceTime(sleep, TimeUnit.NANOSECONDS);
        }
      }
      return ((double) TimeUnit.SECONDS.toNanos(1) * batches * batchSize) / (sleep * batches);
    }
  }
}

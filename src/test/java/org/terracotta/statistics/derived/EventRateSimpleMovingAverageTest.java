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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.CombinableMatcher;
import org.junit.Test;
import org.terracotta.statistics.observer.EventObserver;

import static org.hamcrest.number.IsCloseTo.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author cdennis
 */
public class EventRateSimpleMovingAverageTest {
  
  private static final double EXPECTED_ACCURACY = 0.1;
  
  @Test
  public void testConsistentRate() throws InterruptedException {
    for (int rate = 1; rate < 10; rate++) {
      EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(1, TimeUnit.SECONDS);
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
    
    double firstRate = new EventDriver(stat, 10, 10, 20, TimeUnit.MILLISECONDS).call();
    double lowRate = stat.rate(TimeUnit.SECONDS);
    assertThat(lowRate, closeTo(firstRate, EXPECTED_ACCURACY * firstRate));
    
    double finalRate = new EventDriver(stat, 10, 20, 20, TimeUnit.MILLISECONDS).call();
    double rate = stat.rate(TimeUnit.SECONDS);
    assertThat(rate, CombinableMatcher.<Double>both(greaterThan(lowRate)).and(lessThan(finalRate)));
  }

  @Test
  public void testContinuousRateSplitAcrossTwoThreads() throws InterruptedException {
    EventRateSimpleMovingAverage stat = new EventRateSimpleMovingAverage(1, TimeUnit.SECONDS);
    Callable<Double> c1 = new EventDriver(stat, 10, 20, 20, TimeUnit.MILLISECONDS);
    Callable<Double> c2 = new EventDriver(stat, 10, 20, 20, TimeUnit.MILLISECONDS);

    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      @SuppressWarnings("unchecked")
      List<Future<Double>> futures = executor.invokeAll(Arrays.<Callable<Double>>asList(c1, c2));
      double totalRate = 0f;
      for (Future<Double> rate : futures) {
        try {
          totalRate += rate.get();
        } catch (ExecutionException e) {
          throw new AssertionError(e);
        }
      }
      assertThat(stat.rate(TimeUnit.SECONDS), closeTo(totalRate, EXPECTED_ACCURACY * totalRate));
    } finally {
      executor.shutdown();
    }
  }

  static class EventDriver implements Callable<Double> {

    private final EventObserver stat;
    private final int batches;
    private final int batchSize;
    private final long sleep;
    
    EventDriver(EventObserver stat, int events, long period, TimeUnit unit) {
      this(stat, events, 1, period, unit);
    }
    
    EventDriver(EventObserver stat, int batches, int batchSize, long period, TimeUnit unit) {
      
      this.stat = stat;
      this.batches = batches;
      this.batchSize = batchSize;
      this.sleep = unit.toMillis(period);
    }
    
    @Override
    public Double call() {
      long start = System.nanoTime();
      for (int i = 0; i < batches; i++) {
        try {
          Thread.sleep(sleep);
        } catch (InterruptedException e) {
          throw new AssertionError(e);
        }
        for (int j = 0; j < batchSize; j++) {
          stat.event(0L);
        }
      }
      long end = System.nanoTime();
      return ((double) TimeUnit.SECONDS.toNanos(1) * batches * batchSize) / (end - start);
    }
  }
}

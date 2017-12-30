/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics.ratio;

import org.junit.After;
import org.junit.Test;
import org.terracotta.context.extended.OperationStatisticDescriptor;
import org.terracotta.context.extended.StatisticsRegistry;
import org.terracotta.statistics.archive.Sample;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.of;

/**
 * @author Mathieu Carbou
 */
public class RatioTest {

  ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  Cache cache = new Cache();

  StatisticsRegistry statisticsRegistry = new StatisticsRegistry(
      cache,
      executor,
      1, TimeUnit.MINUTES, // window
      100, // history size
      1, TimeUnit.SECONDS, // sampling frequency
      10, TimeUnit.MINUTES); // TTD

  @After
  public void tearDown() throws Exception {
    statisticsRegistry.clearRegistrations();
    executor.shutdown();
  }

  @Test(timeout = 10000)
  public void test_ratio_calculated() throws Exception {
    OperationStatisticDescriptor<TierOperationOutcomes.GetOutcome> getTierStatisticDescriptor = OperationStatisticDescriptor.descriptor("get", singleton("tier"), TierOperationOutcomes.GetOutcome.class);
    statisticsRegistry.registerCompoundOperations("Hit", getTierStatisticDescriptor, of(TierOperationOutcomes.GetOutcome.HIT));
    statisticsRegistry.registerCompoundOperations("Miss", getTierStatisticDescriptor, of(TierOperationOutcomes.GetOutcome.MISS));
    statisticsRegistry.registerRatios("HitRatio", getTierStatisticDescriptor,
        of(org.terracotta.statistics.ratio.TierOperationOutcomes.GetOutcome.HIT),
        allOf(TierOperationOutcomes.GetOutcome.class));

    // trigger computation
    List<? extends Sample<?>> list = statisticsRegistry.findSampledStatistic("OnHeap:HitRatio").history();
    System.out.println("triggered task scheduling: " + list.size() + " samples");
    long since = System.currentTimeMillis();

    // When testing ratios, we need to wait for the first computation (we do not have any choice) to happen because ratio depends on 2 other sampled statistics.
    // If you do not wait, then you'll always get some NaN because the hits will be done within the 1st second, and the hits won't be done in the right "window".
    // A ratio is computed by dividing a rate with another rate. See CompoundOperationImpl.ratioOf().
    // And a rate is computed with values aggregated into a EventRateSimpleMovingAverage.
    // The call to EventRateSimpleMovingAverage.rateUsingSeconds() will return 0 during the fist second (until first computation did happen).
    // So the hits must be after the first second so that values get accumulated into the partitions of EventRateSimpleMovingAverage.
    Thread.sleep(2000);

    System.out.println("3 puts");
    cache.put("1", "1");
    cache.put("2", "2");
    cache.put("3", "3");

    System.out.println("3 hits");
    cache.get("1"); // hit
    cache.get("2"); // hit
    cache.get("3"); // hit

    do {
      list = statisticsRegistry.findSampledStatistic("OnHeap:HitRatio").history(since);
      System.out.println(list.size() + " samples since " + since);
      since = System.currentTimeMillis();
      for (Sample<?> sample : list) {
        System.out.println(sample.getTimestamp() + " - " + sample.getSample());
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    } while (!Thread.currentThread().isInterrupted() && (list.isEmpty() || !list.get(list.size() - 1).getSample().equals(1d)));
  }

}

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
package org.terracotta.statistics.extended;

import org.junit.Before;
import org.junit.Test;
import org.terracotta.context.extended.OperationStatisticDescriptor;
import org.terracotta.context.extended.ValueStatisticDescriptor;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.observer.OperationObserver;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.terracotta.statistics.StatisticBuilder.operation;
import static org.terracotta.statistics.StatisticsManager.properties;
import static org.terracotta.statistics.StatisticsManager.tags;
import static org.terracotta.statistics.SuppliedValueStatistic.supply;
import static org.terracotta.statistics.extended.SampledStatisticAdapter.sample;
import static org.terracotta.statistics.extended.StatisticType.COUNTER;
import static org.terracotta.statistics.extended.StatisticType.GAUGE;

/**
 * @author Mathieu Carbou
 */
public class StatisticRegistryTest {

  OperationObserver<TimeUnit> timeUnitObserver = operation(TimeUnit.class).named("timeUnit").property("discriminator", "Axis").of(this).tag("axis").build();
  StatisticRegistry registry = new StatisticRegistry(this);

  @Before
  public void setUp() throws Exception {

    StatisticsManager.createPassThroughStatistic(
        this,
        "allocatedMemory",
        tags("OffHeapResource", "tier"),
        properties("discriminator=OffHeapResource", "offHeapResourceIdentifier=primary-server-resource"),
        StatisticType.GAUGE,
        () -> 1024L);

    registry.registerCounter("Cache:Hits", () -> 1L);
    registry.registerGauge("Cache:OffHeapMemoryUsed", () -> 1024L);
    registry.registerSampledStatistic("Cache:GetLatencies", sample(supply(GAUGE, () -> 100L)));
    registry.registerStatistic("Cache:PutLatencies", supply(GAUGE, () -> 200L));
    registry.registerStatistic("Cache:ClearLatencies", GAUGE, () -> 300L);
    assertThat(registry.registerStatistic("AllocatedMemory", ValueStatisticDescriptor.descriptor("allocatedMemory", "tier", "OffHeapResource")), is(true));
    assertThat(registry.registerStatistic("TimeUnit", OperationStatisticDescriptor.descriptor("timeUnit", singleton("axis"), TimeUnit.class), EnumSet.allOf(TimeUnit.class)), is(true));
  }

  @Test
  public void getStatistics() throws Exception {
    assertThat(registry.getStatistics().keySet(), hasItems(
        "Cache:Hits",
        "Cache:OffHeapMemoryUsed",
        "Cache:GetLatencies",
        "Cache:PutLatencies",
        "Cache:ClearLatencies",
        "OffHeapResource:AllocatedMemory",
        "Axis:TimeUnit"));
    assertThat(registry.getStatistics().size(), equalTo(7));
  }

  @Test
  public void queryStatistic() throws Exception {
    assertThat(registry.queryStatistic("Inexisting").isPresent(), is(false));
    Statistic<Serializable> statistic = registry.queryStatistic("Axis:TimeUnit").get();
    assertThat(statistic.getSamples().size(), equalTo(1));
    assertThat(statistic.isEmpty(), is(false));
    assertThat(statistic.getType(), equalTo(COUNTER));
    assertThat(statistic.getLatestSample().get(), equalTo(0L));
  }

  @Test
  public void queryStatistics() throws Exception {
    Map<String, Statistic<? extends Serializable>> statistics = registry.queryStatistics();
    assertThat(statistics.keySet(), hasItems(
        "Cache:Hits",
        "Cache:OffHeapMemoryUsed",
        "Cache:GetLatencies",
        "Cache:PutLatencies",
        "Cache:ClearLatencies",
        "OffHeapResource:AllocatedMemory",
        "Axis:TimeUnit"));
    assertThat(statistics.size(), equalTo(7));
    for (Statistic<? extends Serializable> statistic : statistics.values()) {
      assertThat(statistic.getSamples().size(), equalTo(1));
      assertThat(statistic.isEmpty(), is(false));
      assertThat(statistic.getLatestSample().isPresent(), is(true));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void registerStatistic_duplication() throws Exception {
    registry.registerGauge("Cache:OffHeapMemoryUsed", () -> 2048L);
  }

}
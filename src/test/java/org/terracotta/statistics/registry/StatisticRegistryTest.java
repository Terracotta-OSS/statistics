/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.statistics.registry;

import org.junit.Before;
import org.junit.Test;
import org.terracotta.statistics.StatisticType;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.Time;
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
import static org.terracotta.statistics.SampledStatisticAdapter.sample;
import static org.terracotta.statistics.StatisticBuilder.operation;
import static org.terracotta.statistics.StatisticType.COUNTER;
import static org.terracotta.statistics.StatisticType.GAUGE;
import static org.terracotta.statistics.StatisticsManager.properties;
import static org.terracotta.statistics.StatisticsManager.tags;
import static org.terracotta.statistics.ValueStatistics.gauge;

/**
 * @author Mathieu Carbou
 */
public class StatisticRegistryTest {

  OperationObserver<TimeUnit> timeUnitObserver = operation(TimeUnit.class).named("timeUnit").property("discriminator", "Axis").of(this).tag("axis").build();
  StatisticRegistry registry = new StatisticRegistry(this, Time::absoluteTime);

  @Before
  public void setUp() {

    StatisticsManager.createPassThroughStatistic(
        this,
        "allocatedMemory",
        tags("OffHeapResource", "tier"),
        properties("discriminator=OffHeapResource", "offHeapResourceIdentifier=primary-server-resource"),
        StatisticType.GAUGE,
        () -> 1024L);

    registry.registerCounter("Cache:Hits", () -> 1L);
    registry.registerGauge("Cache:OffHeapMemoryUsed", () -> 1024L);
    registry.registerStatistic("Cache:GetLatencies", sample(gauge(() -> 100L), Time::absoluteTime));
    registry.registerStatistic("Cache:PutLatencies", gauge(() -> 200L));
    registry.registerStatistic("Cache:ClearLatencies", GAUGE, () -> 300L);
    assertThat(registry.registerStatistic("AllocatedMemory", ValueStatisticDescriptor.descriptor("allocatedMemory", "tier", "OffHeapResource")), is(true));
    assertThat(registry.registerStatistic("TimeUnit", OperationStatisticDescriptor.descriptor("timeUnit", singleton("axis"), TimeUnit.class), EnumSet.allOf(TimeUnit.class)), is(true));
  }

  @Test
  public void queryStatistic() {
    assertThat(registry.queryStatistic("Inexisting").isPresent(), is(false));
    Statistic<Serializable> statistic = registry.queryStatistic("Axis:TimeUnit").get();
    assertThat(statistic.getSamples().size(), equalTo(1));
    assertThat(statistic.isEmpty(), is(false));
    assertThat(statistic.getType(), equalTo(COUNTER));
    assertThat(statistic.getLatestSampleValue().get(), equalTo(0L));
  }

  @Test
  public void queryStatistics() {
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
      assertThat(statistic.getLatestSampleValue().isPresent(), is(true));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void registerStatistic_duplication() {
    registry.registerGauge("Cache:OffHeapMemoryUsed", () -> 2048L);
  }

}
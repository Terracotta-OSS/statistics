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
package org.terracotta.statistics;

import org.junit.Before;
import org.junit.Test;
import org.terracotta.statistics.registry.StatisticRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.terracotta.statistics.StatisticType.COUNTER;
import static org.terracotta.statistics.StatisticType.GAUGE;
import static org.terracotta.statistics.ValueStatistics.counter;

public class TableStatisticTest {

  @Test
  public void test_returning_live_independent_stats() throws Exception {
    StatisticRegistry registry = new StatisticRegistry(this);

    // fetching the live stats
    Map<String, Map<String, Supplier<Number>>> liveDbStats = getLiveDbStats();

    // create a "live" table statistic
    ValueStatistic<Table> statistic = TableValueStatistic.newBuilder("total-exec-count", "total-failed-count")
        .withRows(liveDbStats.keySet(), (queryId, rowBuilder) -> rowBuilder
            .registerStatistic("total-exec-count", counter(liveDbStats.get(queryId).get("total-exec-count")))
            .registerStatistic("total-failed-count", counter(liveDbStats.get(queryId).get("total-failed-count"))))
        .build();

    registry.registerStatistic("Database:TopQueries", statistic);

    Table table = registry.<Table>queryStatistic("Database:TopQueries").get().getLatestSampleValue().get();
    assertThat(table.getRowCount(), equalTo(2));
    assertTable(table);
  }

  @Test
  public void test_returning_dependent_stats_snapshot() throws Exception {
    StatisticRegistry registry = new StatisticRegistry(this);

    // create a statistic that is fetched when queried
    registry.registerTable("Database:TopQueries", () -> {

      // atomically fetch the set of statistic from backend layer 
      Map<String, Map<String, Number>> snapshot = getDbStatsSnapshot();

      return Table.newBuilder("total-exec-count", "total-failed-count", "total-exec-time")
          .withRows(snapshot.keySet(), (queryId, rowBuilder) -> rowBuilder
              .setStatistic("total-exec-count", COUNTER, snapshot.get(queryId).get("total-exec-count"))
              .setStatistic("total-failed-count", COUNTER, snapshot.get(queryId).get("total-failed-count"))
              .setStatistic("total-exec-time", GAUGE, snapshot.get(queryId).get("total-exec-time")))
          .build();
    });

    Table table = registry.<Table>queryStatistic("Database:TopQueries").get().getLatestSampleValue().get();
    assertThat(table.getRowCount(), equalTo(2));
    assertTable(table);

    assertThat(Arrays.asList(table.getStatisticNames()), hasItems("total-exec-count", "total-failed-count", "total-exec-time"));

    assertThat(table.<Long>getStatistic("prepared-query-1", "total-exec-time").get().value(), is(1000L));
    assertThat(table.<Long>getStatistic("prepared-query-1", "total-exec-time").get().type(), is(StatisticType.GAUGE));
  }

  private void assertTable(Table table) {
    assertThat(table.getRowLabels(), hasItems("prepared-query-1", "prepared-query-2"));

    assertThat(table.<Long>getStatistic("prepared-query-1", "total-exec-count").get().value(), is(10L));
    assertThat(table.<Long>getStatistic("prepared-query-1", "total-exec-count").get().type(), is(StatisticType.COUNTER));

    assertThat(table.<Long>getStatistic("prepared-query-1", "total-failed-count").get().value(), is(1L));
    assertThat(table.<Long>getStatistic("prepared-query-1", "total-failed-count").get().type(), is(StatisticType.COUNTER));

    assertThat(table.<Long>getStatistic("prepared-query-2", "total-exec-count").get().value(), is(20L));
    assertThat(table.<Long>getStatistic("prepared-query-2", "total-exec-count").get().type(), is(StatisticType.COUNTER));

    assertThat(table.<Long>getStatistic("prepared-query-2", "total-failed-count").get().value(), is(2L));
    assertThat(table.<Long>getStatistic("prepared-query-2", "total-failed-count").get().type(), is(StatisticType.COUNTER));
  }

  private Map<String, Map<String, Supplier<Number>>> dbStats = new HashMap<>();

  private Map<String, Map<String, Number>> getDbStatsSnapshot() {
    return dbStats.entrySet()
        .stream()
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().entrySet()
            .stream()
            .collect(toMap(Map.Entry::getKey, ee -> ee.getValue().get()))));
  }

  private Map<String, Map<String, Supplier<Number>>> getLiveDbStats() {
    return dbStats;
  }

  @Before
  public void setUp() throws Exception {
    dbStats.put("prepared-query-1", new HashMap<String, Supplier<Number>>() {{
      put("total-exec-count", () -> 10L);
      put("total-exec-time", () -> 1000L);
      put("total-failed-count", () -> 1L);
    }});
    dbStats.put("prepared-query-2", new HashMap<String, Supplier<Number>>() {{
      put("total-exec-count", () -> 20L);
      put("total-exec-time", () -> 2000L);
      put("total-failed-count", () -> 2L);
    }});
  }

}

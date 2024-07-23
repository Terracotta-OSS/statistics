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
package org.terracotta.statistics;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A statistic that can dynamically construct and provide a table statistic based on provided suppliers
 *
 * @author Mathieu Carbou
 */
public class TableValueStatistic extends TableSkeleton implements ValueStatistic<Table> {

  private static final long serialVersionUID = 1L;

  public static TableValueStatistic.Builder newBuilder(String... innerStatisticNames) {
    return new TableValueStatistic.Builder(innerStatisticNames);
  }

  private TableValueStatistic(String... innerStatisticNames) {
    super(innerStatisticNames);
  }

  @Override
  public StatisticType type() {
    return StatisticType.TABLE;
  }

  @Override
  public Table value() {
    String[] statisticNames = getStatisticNames();
    return Table.newBuilder(statisticNames)
        .withRows(getRowLabels(), (row, rowBuilder) -> {
          ValueStatistic<? extends Serializable>[] vals = getStatistics(row);
          for (int i = 0; i < vals.length; i++) {
            rowBuilder.setStatistic(statisticNames[i], vals[i].type(), vals[i].value());
          }
        })
        .build();
  }

  public static class Builder {

    private final TableValueStatistic stat;

    private Builder(String... innerStatisticNames) {
      stat = new TableValueStatistic(innerStatisticNames);
    }

    public <T extends Serializable> Builder registerStatistic(String rowName, String statisticName, ValueStatistic<T> accessor) {
      stat.insert(rowName, statisticName, accessor);
      return this;
    }

    public Builder withRow(String rowName, Consumer<RowBuilder> c) {
      c.accept(new RowBuilder() {
        @Override
        public <T extends Serializable> RowBuilder registerStatistic(String statisticName, ValueStatistic<T> accessor) {
          stat.insert(rowName, statisticName, accessor);
          return this;
        }
      });
      return this;
    }

    public Builder withRows(Collection<String> rowNames, BiConsumer<String, RowBuilder> c) {
      rowNames.forEach(rowName -> c.accept(rowName, new RowBuilder() {
        @Override
        public <T extends Serializable> RowBuilder registerStatistic(String statisticName, ValueStatistic<T> accessor) {
          stat.insert(rowName, statisticName, accessor);
          return this;
        }
      }));
      return this;
    }

    public ValueStatistic<Table> build() {
      return stat;
    }
  }

  public interface RowBuilder {
    <T extends Serializable> RowBuilder registerStatistic(String statisticName, ValueStatistic<T> accessor);
  }

}

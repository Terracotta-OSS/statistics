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
 * A table statistic contains for each row a set of statistics.
 * <p>
 * Example: The table statistic TopQueries can contain for each prepared statement the total execution time and the count.
 *
 * @author Mathieu Carbou
 */
public class Table extends TableSkeleton implements Serializable {

  private static final long serialVersionUID = 1L;

  public static Builder newBuilder(String... innerStatisticNames) {
    return new Builder(innerStatisticNames);
  }

  private Table(String... innerStatisticNames) {
    super(innerStatisticNames);
  }

  public static class Builder {

    private final Table table;

    private Builder(String... innerStatisticNames) {
      table = new Table(innerStatisticNames);
    }

    public <T extends Serializable> Table.Builder setStatistic(String rowName, String statisticName, StatisticType type, T value) {
      table.insert(rowName, statisticName, type, value);
      return this;
    }

    public Table.Builder withRow(String rowName, Consumer<Table.RowBuilder> c) {
      c.accept(new Table.RowBuilder() {
        @Override
        public <T extends Serializable> Table.RowBuilder setStatistic(String statisticName, StatisticType type, T value) {
          table.insert(rowName, statisticName, type, value);
          return this;
        }
      });
      return this;
    }

    public Table.Builder withRows(Collection<String> rowNames, BiConsumer<String, Table.RowBuilder> c) {
      rowNames.forEach(rowName -> c.accept(rowName, new Table.RowBuilder() {
        @Override
        public <T extends Serializable> Table.RowBuilder setStatistic(String statisticName, StatisticType type, T value) {
          table.insert(rowName, statisticName, type, value);
          return this;
        }
      }));
      return this;
    }

    public Table build() {
      return table;
    }
  }

  public interface RowBuilder {
    <T extends Serializable> RowBuilder setStatistic(String statisticName, StatisticType type, T value);
  }

}

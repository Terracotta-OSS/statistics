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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.terracotta.statistics.ValueStatistics.constant;

/**
 * @author Mathieu Carbou
 */
public abstract class TableSkeleton implements Serializable {

  private static final long serialVersionUID = 1L;

  // keep row ordering
  private final Map<String, ValueStatistic<? extends Serializable>[]> statistics = new LinkedHashMap<>();
  private final String[] innerStatisticNames;

  protected TableSkeleton(String... innerStatisticNames) {
    this.innerStatisticNames = innerStatisticNames.clone();
    Arrays.sort(this.innerStatisticNames);
  }

  public int getRowCount() {
    return statistics.size();
  }

  public Collection<String> getRowLabels() {
    return statistics.keySet();
  }

  public String[] getStatisticNames() {
    return innerStatisticNames.clone(); // because of findbug check (EI_EXPOSE_REP)
  }

  public int getStatisticCount() {
    return innerStatisticNames.length;
  }

  public ValueStatistic<? extends Serializable>[] getStatistics(String row) {
    return statistics.get(row);
  }

  public Map<String, ValueStatistic<? extends Serializable>[]> getStatistics() {
    return statistics;
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> Optional<ValueStatistic<T>> getStatistic(String row, String statisticName) {
    ValueStatistic<? extends Serializable>[] statistics = this.statistics.get(row);
    if (statistics == null) {
      return Optional.empty();
    }
    int idx = Arrays.binarySearch(innerStatisticNames, statisticName);
    if (idx < 0) {
      return Optional.empty();
    }
    return Optional.of((ValueStatistic<T>) statistics[idx]);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "statistics=" + statistics +
        ", rowCount=" + getRowCount() +
        ", rowLabels=" + getRowLabels() +
        '}';
  }

  @SuppressWarnings("unchecked")
  protected <T extends Serializable> void insert(String rowName, String statisticName, StatisticType type, T value) {
    insert(rowName, statisticName, constant(type, value));
  }

  @SuppressWarnings("unchecked")
  protected <T extends Serializable> void insert(String rowName, String statisticName, ValueStatistic<T> accessor) {
    int idx = Arrays.binarySearch(innerStatisticNames, statisticName);
    if (idx < 0) {
      throw new IllegalArgumentException("Illegal inner statistic: " + statisticName + ". Allowed: " + Arrays.asList(innerStatisticNames));
    }
    ValueStatistic<? extends Serializable>[] accessors = statistics.get(rowName);
    if (accessors == null) {
      accessors = new ValueStatistic[innerStatisticNames.length];
      statistics.put(rowName, accessors);
    }
    accessors[idx] = accessor;
  }
}

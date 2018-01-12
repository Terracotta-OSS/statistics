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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.terracotta.statistics.StatisticType.COUNTER;
import static org.terracotta.statistics.StatisticType.GAUGE;
import static org.terracotta.statistics.StatisticType.RATE;
import static org.terracotta.statistics.StatisticType.RATIO;
import static org.terracotta.statistics.StatisticType.TABLE;

/**
 * @author Mathieu Carbou
 */
public class ValueStatistics {

  public static <T extends Serializable> ConstantValueStatistic<T> nullValue(StatisticType type) {
    return new ConstantValueStatistic<>(type, null);
  }

  public static <T extends Serializable> ConstantValueStatistic<T> constant(StatisticType type, T value) {
    return new ConstantValueStatistic<>(type, value);
  }

  public static <T extends Serializable> ValueStatistic<T> supply(StatisticType type, Supplier<T> supplier) {
    return new SuppliedValueStatistic<>(type, supplier);
  }

  public static <T extends Number> ValueStatistic<T> gauge(Supplier<T> supplier) {
    return supply(GAUGE, supplier);
  }

  public static <T extends Number> ValueStatistic<T> counter(Supplier<T> supplier) {
    return supply(COUNTER, supplier);
  }

  public static <T extends Number> ValueStatistic<T> rate(Supplier<T> supplier) {
    return supply(RATE, supplier);
  }

  public static <T extends Number> ValueStatistic<T> ratio(Supplier<T> supplier) {
    return supply(RATIO, supplier);
  }

  public static <T extends Table> ValueStatistic<T> table(Supplier<T> supplier) {
    return supply(TABLE, supplier);
  }

  /**
   * Returns a {@link ValueStatistic} that caches the value of a statistic for at least a specific amount of time.
   * <p>
   * This method does not block.
   * <p>
   * When the delay expires, if several threads are coming at the same time to read the expired value, then only one will
   * do the update and set a new expiring delay and read the new value.
   * The other threads can continue to read the current expired value for their next call until it gets updated.
   * <p>
   * If the caching delay is smaller than the time it takes for a statistic value to be computed, then it is possible that
   * a new thread starts asking for a new value before the previous update is completed. In this case, there is no guarantee
   * that the cached value will be updated in order because it depends on the time took to get the new value.
   *
   * @param delay          The delay
   * @param unit           The unit of time
   * @param valueStatistic The delegate statistic that will provide the value
   * @param <T>            The statistic type
   * @return the memoizing statistic
   */
  public static <T extends Serializable> ValueStatistic<T> memoize(long delay, TimeUnit unit, ValueStatistic<T> valueStatistic) {
    return new MemoizingValueStatistic<>(delay, unit, valueStatistic);
  }
}

/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company.
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
package org.terracotta.statistics;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An operation observer that tracks operation result counts and can drive further derived statistics.
 * <p>
 * {@link #begin()} and {@link #end(Enum)} must be called from the same thread so that latency can be computed.
 *
 * @param <T> the operation result enum type
 */
class GeneralOperationStatistic<T extends Enum<T>> extends AbstractOperationStatistic<T> implements OperationStatistic<T> {

  private final LongAdder[] counts;

  /**
   * Create an operation statistics for a given operation result type.
   *
   * @param properties a set of context properties
   * @param type       operation result type
   */
  GeneralOperationStatistic(String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> type) {
    super(name, tags, properties, type);
    this.counts = new LongAdder[type.getEnumConstants().length];
    for (int i = 0; i < counts.length; i++) {
      counts[i] = new LongAdder();
    }
  }

  /**
   * Return the count of operations with the given type.
   *
   * @param type the result type
   * @return the operation count
   */
  @Override
  public long count(T type) {
    return counts[type.ordinal()].sum();
  }

  @Override
  public long sum(Set<T> types) {
    long sum = 0;
    for (T t : types) {
      sum += counts[t.ordinal()].sum();
    }
    return sum;
  }

  @Override
  public void end(T result) {
    counts[result.ordinal()].increment();
    super.end(result);
  }

  @Override
  public String toString() {
    T[] constants = type.getEnumConstants();

    return IntStream.range(0, constants.length)
        .mapToObj(i -> constants[i] + "=" + counts[i])
        .collect(Collectors.joining(", ", "[", "]"));
  }
}

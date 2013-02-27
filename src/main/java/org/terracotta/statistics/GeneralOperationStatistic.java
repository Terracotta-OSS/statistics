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
package org.terracotta.statistics;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.terracotta.statistics.jsr166e.LongAdder;
import org.terracotta.statistics.observer.OperationObserver;
import org.terracotta.statistics.observer.ChainedOperationObserver;

/**
 * An operation observer that tracks operation result counts and can drive further derived statistics.
 * 
 * @param <T> the operation result enum type
 */
class GeneralOperationStatistic<T extends Enum<T>> extends AbstractOperationStatistic<T> implements OperationStatistic<T> {
  
  private final EnumMap<T, LongAdder> counts;
  
  /**
   * Create an operation statistics for a given operation result type.
   * 
   * @param properties a set of context properties
   * @param type operation result type
   */
  GeneralOperationStatistic(String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> type) {
    super(name, tags, properties, type);
    this.counts = new EnumMap<T, LongAdder>(type);
    for (T t : type.getEnumConstants()) {
      counts.put(t, new LongAdder());
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
    return counts.get(type).sum();
  }

  @Override
  public long sum(Set<T> types) {
    long sum = 0;
    for (T t : types) {
      sum += counts.get(t).sum();
    }
    return sum;
  }
  
  @Override
  public void end(T result) {
    counts.get(result).increment();
    if (!derivedStatistics.isEmpty()) {
      long time = Time.time();
      for (ChainedOperationObserver<? super T> observer : derivedStatistics) {
        observer.end(time, result);
      }
    }
  }
  
  @Override
  public void end(T result, long ... parameters) {
    counts.get(result).increment();
    if (!derivedStatistics.isEmpty()) {
      long time = Time.time();
      for (ChainedOperationObserver<? super T> observer : derivedStatistics) {
        observer.end(time, result, parameters);
      }
    }
  }
  
  @Override
  public String toString() {
    return counts.toString();
  }
}

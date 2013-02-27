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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.statistics.observer.ChainedOperationObserver;

/**
 *
 * @author cdennis
 */
@ContextAttribute("this")
public abstract class AbstractOperationStatistic<T extends Enum<T>> extends AbstractSourceStatistic<ChainedOperationObserver<? super T>> implements OperationStatistic<T> {

  @ContextAttribute("name") public final String name;
  @ContextAttribute("tags") public final Set<String> tags;
  @ContextAttribute("properties") public final Map<String, Object> properties;
  @ContextAttribute("type") public final Class<T> type;
  
  /**
   * Create an operation statistics for a given operation result type.
   * 
   * @param properties a set of context properties
   * @param type operation result type
   */
  AbstractOperationStatistic(String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> type) {
    this.name = name;
    this.tags = Collections.unmodifiableSet(new HashSet<String>(tags));
    this.properties = Collections.unmodifiableMap(new HashMap<String, Object>(properties));
    this.type = type;
  }
  
  @Override
  public Class<T> type() {
    return type;
  }
  
  /**
   * Return a {@ValueStatistic<Long>} returning the count for the given result.
   * 
   * @param result the result of interest 
   * @return a {@code ValueStatistic} instance
   */
  @Override
  public ValueStatistic<Long> statistic(final T result) {
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return count(result);
      }
    };
  }

  @Override
  public ValueStatistic<Long> statistic(final Set<T> results) {
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return sum(results);
      }
    };
  }
  
  @Override
  public long sum() {
    return sum(EnumSet.allOf(type));
  }
  
  @Override
  public void begin() {
    if (!derivedStatistics.isEmpty()) {
      long time = Time.time();
      for (ChainedOperationObserver<? super T> observer : derivedStatistics) {
        observer.begin(time);
      }
    }
  }
}

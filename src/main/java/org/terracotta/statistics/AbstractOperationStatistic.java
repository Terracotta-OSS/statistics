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

import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.statistics.observer.ChainedOperationObserver;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cdennis
 * @implNote {@link #begin()} and {@link #end(Enum)} must be called from the same thread so that latency can be computed.
 */
@ContextAttribute("this")
public abstract class AbstractOperationStatistic<T extends Enum<T>> extends AbstractSourceStatistic<ChainedOperationObserver<? super T>> implements OperationStatistic<T> {

  @ContextAttribute("name") public final String name;
  @ContextAttribute("tags") public final Set<String> tags;
  @ContextAttribute("properties") public final Map<String, Object> properties;
  @ContextAttribute("type") public final Class<T> type;

  private final ThreadLocal<Long> operationStartTime = new ThreadLocal<>();

  /**
   * Create an operation statistics for a given operation result type.
   *
   * @param properties a set of context properties
   * @param type       operation result type
   */
  AbstractOperationStatistic(String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> type) {
    this.name = name;
    this.tags = Collections.unmodifiableSet(new HashSet<>(tags));
    this.properties = Collections.unmodifiableMap(new HashMap<String, Object>(properties));
    this.type = type;
  }

  @Override
  public Class<T> type() {
    return type;
  }

  @Override
  public long sum() {
    return sum(EnumSet.allOf(type));
  }

  @Override
  public void begin() {
    if (!derivedStatistics.isEmpty()) {
      long time = Time.time();
      operationStartTime.set(time);
      for (ChainedOperationObserver<? super T> observer : derivedStatistics) {
        observer.begin(time);
      }
    }
  }

  @Override
  public void end(T result) {
    if (!derivedStatistics.isEmpty()) {
      long time = Time.time();
      long latency = time - operationStartTime.get();
      for (ChainedOperationObserver<? super T> observer : derivedStatistics) {
        observer.end(time, latency, result);
      }
    }
  }

}

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

import java.util.Set;

import org.terracotta.statistics.observer.ChainedOperationObserver;
import org.terracotta.statistics.observer.OperationObserver;

/**
 *
 * @author cdennis
 */
public interface OperationStatistic<T extends Enum<T>> extends OperationObserver<T>, SourceStatistic<ChainedOperationObserver<? super T>> {

  public Class<T> type();
  
  /**
   * Return a {@ValueStatistic<Long>} returning the count for the given result.
   * 
   * @param result the result of interest 
   * @return a {@code ValueStatistic} instance
   */
  public ValueStatistic<Long> statistic(T result);

  public ValueStatistic<Long> statistic(Set<T> results);
  
  /**
   * Return the count of operations with the given type.
   * 
   * @param type the result type
   * @return the operation count
   */
  public long count(T type);

  public long sum(Set<T> types);
  
  public long sum();
}

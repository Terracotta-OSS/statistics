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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.terracotta.context.ContextElement;
import org.terracotta.context.ContextManager;
import org.terracotta.context.TreeNode;
import org.terracotta.statistics.observer.OperationObserver;

public class StatisticsManager extends ContextManager {

  public static <T extends Enum<T>> OperationObserver<T> createOperationStatistic(Object context, String name, Set<String> tags, Class<T> eventTypes) {
    return createOperationStatistic(context, name, tags, Collections.<String, Object>emptyMap(), eventTypes);
  }
  
  public static <T extends Enum<T>> OperationObserver<T> createOperationStatistic(Object context, String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> resultType) {
    OperationStatistic<T> stat = createOperationStatistic(name, tags, properties, resultType);
    associate(context).withChild(stat);
    return stat;
  }

  private static <T extends Enum<T>> OperationStatistic<T> createOperationStatistic(String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> resultType) {
    return new GeneralOperationStatistic<T>(name, tags, properties, resultType);
  }
  
  public static <T extends Enum<T>> OperationStatistic<T> getOperationStatisticFor(OperationObserver<T> observer) {
    TreeNode node = ContextManager.nodeFor(observer);
    if (node == null) {
      return null;
    } else {
      ContextElement context = node.getContext();
      if (OperationStatistic.class.isAssignableFrom(context.identifier())) {
        return (OperationStatistic<T>) context.attributes().get("this");
      } else {
        throw new AssertionError();
      }
    }
  }
  
  public static <T extends Number> ValueStatistic<T> createPassThroughStatistic(Object context, String name, Set<String> tags, Callable<T> source) {
    return createPassThroughStatistic(context, name, tags, Collections.<String, Object>emptyMap(), source);
  }
  
  public static <T extends Number> ValueStatistic<T> createPassThroughStatistic(Object context, String name, Set<String> tags, Map<String, ? extends Object> properties, Callable<T> source) {
    PassThroughStatistic<T> stat = new PassThroughStatistic<T>(context, name, tags, properties, source);
    associate(context).withChild(stat);
    return stat;
  }
}

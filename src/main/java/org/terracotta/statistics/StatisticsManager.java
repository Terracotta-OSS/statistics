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


import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.terracotta.context.ContextElement;
import org.terracotta.context.ContextManager;
import org.terracotta.context.ContextManager.Association;
import org.terracotta.context.ContextManager.Dissociation;
import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.observer.OperationObserver;

public class StatisticsManager {
  
  private final ContextManager contextManager = new ContextManager();
  
  public static <T extends Enum<T>> OperationObserver<T> createOperationStatistic(Object context, Map<String, ? extends Object> properties, Class<T> eventTypes) {
    OperationStatistic<T> stat = new OperationStatistic<T>(properties, eventTypes);
    associate(context).withChild(stat);
    return stat;
  }
  
  public static <T extends Enum<T>> OperationStatistic<T> getOperationStatisticFor(OperationObserver<T> observer) {
    TreeNode node = ContextManager.nodeFor(observer);
    if (node == null) {
      return null;
    } else {
      ContextElement context = node.getContext();
      if (OperationStatistic.class.equals(context.identifier())) {
        return (OperationStatistic<T>) context.attributes().get("this");
      } else {
        throw new AssertionError();
      }
    }
  }
  
  public static <T extends Number> void createPassThroughStatistic(Object context, Callable<T> source) {
    PassThroughStatistic<T> stat = new PassThroughStatistic<T>(source);
    associate(context).withChild(stat);
  }
  
  public Collection<? extends TreeNode> query(Query query) {
    return contextManager.query(query);
  }
  
  public TreeNode queryForSingleton(Query query) {
    return contextManager.queryForSingleton(query);
  }
        
  public void root(Object object) {
    contextManager.root(object);
  }
  
  public static Association associate(Object obj) {
    return ContextManager.associate(obj);
  }

  public static Dissociation dissociate(Object obj) {
    return ContextManager.dissociate(obj);
  }
}

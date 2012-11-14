/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;


import java.util.Collection;
import java.util.concurrent.Callable;

import org.terracotta.context.ContextManager;
import org.terracotta.context.ContextManager.Association;
import org.terracotta.context.ContextManager.Dissociation;
import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.impl.OperationStatisticImpl;
import org.terracotta.statistics.impl.PassThroughStatistic;
import org.terracotta.statistics.observer.OperationObserver;

public class StatisticsManager {
  
  private final ContextManager contextManager = new ContextManager();
  
  enum GetEventType {
    HIT, MISS;
  }
  
  public static <T extends Enum<T>> OperationObserver<T> createOperationStatistic(Object context, String name, Class<T> eventTypes) {
    OperationStatisticImpl stat = new OperationStatisticImpl<T>(name, eventTypes);
    associate(context).withChild(stat);
    return stat;
  }
  
  public static <T extends Number> void createPassThroughStatistic(Object context, Callable<T> source) {
    PassThroughStatistic<T> stat = new PassThroughStatistic<T>(source);
    associate(context).withChild(stat);
  }
  
  public Collection<? extends TreeNode<Class, String, Object>> query(Query query) {
    return contextManager.query(query);
  }
  
  public TreeNode<Class, String, Object> queryForSingleton(Query query) {
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

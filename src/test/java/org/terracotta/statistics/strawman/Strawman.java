/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.strawman;

import java.util.Arrays;

import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.OperationStatistic;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.impl.InlineLatencyDistribution;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.strawman.Cache.GetResult;

import static org.hamcrest.collection.IsMapContaining.*;
import static org.hamcrest.core.CombinableMatcher.*;
import static org.terracotta.context.query.QueryBuilder.*;

public final class Strawman {
  
  public static void main(String[] args) {
    CacheManager manager = new CacheManager("manager-one");
    Cache<String, String> cache = new Cache("cache-one");
    
    manager.addCache(cache);

    StatisticsManager stats = new StatisticsManager();
    stats.root(manager);

    Query query = queryBuilder().descendants().filter(context(both(identifier(subclassOf(OperationStatistic.class))).and(attributes(hasEntry("name", "get"))))).build();
    System.out.println(query);
    TreeNode<Class, String, Object> getStatisticNode = stats.queryForSingleton(query);
    OperationStatistic<GetResult> getStatistic = (OperationStatistic<GetResult>) getStatisticNode.getContext().attributes().get("this");
    InlineLatencyDistribution<Cache.GetResult> hitLatency = new InlineLatencyDistribution(Cache.GetResult.HIT);
    getStatistic.addDerivedStatistic(hitLatency);
    
    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatency.mean());
    
    cache.put("foo", "bar");
    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatency.mean());

    hitLatency.addDerivedStatistic(new EventObserver() {

      @Override
      public void event(long parameter) {
        System.out.println("Event Latency : " + parameter);
      }
    });
    
    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatency.mean());
    
    getStatistic.removeDerivedStatistic(hitLatency);

    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatency.mean());
  }
  
  public static String dumpTree(TreeNode<?, ?, ?> node) {
    return dumpSubtree(0, node);
  }
  
  public static String dumpSubtree(int indent, TreeNode<?, ?, ?> node) {
    char[] indentChars = new char[indent];
    Arrays.fill(indentChars, ' ');
    StringBuilder sb = new StringBuilder();
    String nodeString = node.toString();
    sb.append(indentChars).append(nodeString).append("\n");
    for (TreeNode<?, ?, ?> child : node.getChildren()) {
      sb.append(dumpSubtree(indent + nodeString.length(), child));
    }
    return sb.toString();
  }
}

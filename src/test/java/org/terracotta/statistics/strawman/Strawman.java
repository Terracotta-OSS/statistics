/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.strawman;

import java.util.Arrays;

import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.OperationStatistic;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.derived.LatencySampling;
import org.terracotta.statistics.derived.MinMaxAverage;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.strawman.Cache.GetResult;

import static org.terracotta.context.query.Matchers.*;
import static org.terracotta.context.query.QueryBuilder.*;

public final class Strawman {
  
  public static void main(String[] args) {
    CacheManager manager = new CacheManager("manager-one");
    Cache<String, String> cache = new Cache("cache-one");
    
    manager.addCache(cache);

    StatisticsManager stats = new StatisticsManager();
    stats.root(manager);

    Query query = queryBuilder().descendants().filter(context(allOf(identifier(subclassOf(OperationStatistic.class)), attributes(hasAttribute("properties", hasAttribute("name", "get")))))).build();
    System.out.println(query);
    TreeNode<Class, String, Object> getStatisticNode = stats.queryForSingleton(query);
    OperationStatistic<GetResult> getStatistic = (OperationStatistic<GetResult>) getStatisticNode.getContext().attributes().get("this");
    LatencySampling<Cache.GetResult> hitLatency = new LatencySampling(Cache.GetResult.HIT, 1.0f);
    MinMaxAverage hitLatencyStats = new MinMaxAverage();
    hitLatency.addDerivedStatistic(hitLatencyStats);
    getStatistic.addDerivedStatistic(hitLatency);
    
    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.mean());
    
    cache.put("foo", "bar");
    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.mean());

    hitLatency.addDerivedStatistic(new EventObserver() {

      @Override
      public void event(long parameter) {
        System.out.println("Event Latency : " + parameter);
      }
    });
    
    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.mean());
    
    getStatistic.removeDerivedStatistic(hitLatency);

    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.mean());
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

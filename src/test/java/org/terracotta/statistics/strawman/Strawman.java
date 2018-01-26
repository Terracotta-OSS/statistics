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
package org.terracotta.statistics.strawman;

import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Matchers;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.OperationStatistic;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.derived.OperationResultSampler;
import org.terracotta.statistics.derived.latency.LatencyAccumulator;

import java.util.Arrays;

import static java.util.EnumSet.of;
import static org.terracotta.context.query.Matchers.attributes;
import static org.terracotta.context.query.Matchers.context;
import static org.terracotta.context.query.Matchers.hasAttribute;
import static org.terracotta.context.query.Matchers.identifier;
import static org.terracotta.context.query.Matchers.subclassOf;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;

public final class Strawman {

  public static void main(String[] args) {
    CacheManager manager = new CacheManager("manager-one");
    Cache<String, String> cache = new Cache<>("cache-one");

    manager.addCache(cache);

    StatisticsManager stats = new StatisticsManager();
    stats.root(manager);

    @SuppressWarnings("unchecked")
    Query query = queryBuilder().descendants().filter(context(Matchers.allOf(identifier(subclassOf(OperationStatistic.class)), attributes(hasAttribute("name", "get"))))).build();
    System.out.println(query);
    TreeNode getStatisticNode = stats.queryForSingleton(query);

    @SuppressWarnings("unchecked")
    OperationStatistic<GetResult> getStatistic = (OperationStatistic<GetResult>) getStatisticNode.getContext().attributes().get("this");
    OperationResultSampler<GetResult> hitLatency = new OperationResultSampler<>(of(GetResult.HIT), 1.0f);
    LatencyAccumulator hitLatencyStats = LatencyAccumulator.empty();
    hitLatency.addDerivedStatistic(hitLatencyStats);
    getStatistic.addDerivedStatistic(hitLatency);

    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.average());

    cache.put("foo", "bar");
    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.average());

    hitLatency.addDerivedStatistic((time, latency) -> System.out.println("Event Latency : " + latency));

    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.average());

    getStatistic.removeDerivedStatistic(hitLatency);

    cache.get("foo");
    System.err.println("HITS        : " + getStatistic.count(GetResult.HIT));
    System.err.println("MISSES      : " + getStatistic.count(GetResult.MISS));
    System.err.println("HIT LATENCY : " + hitLatencyStats.average());
  }

  public static String dumpTree(TreeNode node) {
    return dumpSubtree(0, node);
  }

  public static String dumpSubtree(int indent, TreeNode node) {
    char[] indentChars = new char[indent];
    Arrays.fill(indentChars, ' ');
    StringBuilder sb = new StringBuilder();
    String nodeString = node.toString();
    sb.append(indentChars).append(nodeString).append("\n");
    for (TreeNode child : node.getChildren()) {
      sb.append(dumpSubtree(indent + nodeString.length(), child));
    }
    return sb.toString();
  }
}

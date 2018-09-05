/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics;

import org.terracotta.context.ContextManager;
import org.terracotta.context.TreeNode;
import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.context.query.Matcher;
import org.terracotta.context.query.Matchers;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.observer.ChainedOperationObserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.terracotta.context.query.Matchers.attributes;
import static org.terracotta.context.query.Matchers.context;
import static org.terracotta.context.query.Matchers.hasAttribute;
import static org.terracotta.context.query.Matchers.identifier;
import static org.terracotta.context.query.Matchers.subclassOf;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;

@ContextAttribute("this")
public class MappedOperationStatistic<S extends Enum<S>, D extends Enum<D>> implements OperationStatistic<D> {

  @ContextAttribute("name") public final String name;
  @ContextAttribute("tags") public final Set<String> tags;
  @ContextAttribute("properties") public final Map<String, Object> properties;
  @ContextAttribute("type") public final Class<D> outcomeType;

  private final StatisticMapper<S, D> mapper;

  public MappedOperationStatistic(Object tier, Map<D, Set<S>> translation, String statisticName, int tierHeight, String targetName, String discriminator) {

    this.name = statisticName;
    this.tags = Collections.singleton("tier");
    this.properties = new HashMap<>();
    this.properties.put("tierHeight", tierHeight);
    this.properties.put("discriminator", discriminator);

    Entry<D, Set<S>> first = translation.entrySet().iterator().next();
    Class<S> outcomeType = first.getValue().iterator().next().getDeclaringClass();
    this.outcomeType = first.getKey().getDeclaringClass();

    this.mapper = new StatisticMapper<>(translation, findOperationStat(tier, outcomeType, targetName));
  }

  @Override
  public Class<D> type() {
    return outcomeType;
  }

  @Override
  public ValueStatistic<Long> statistic(D result) {
    return mapper.statistic(result);
  }

  @Override
  public ValueStatistic<Long> statistic(Set<D> results) {
    return mapper.statistic(results);
  }

  @Override
  public long count(D type) {
    return mapper.count(type);
  }

  @Override
  public long sum(Set<D> types) {
    return mapper.sum(types);
  }

  @Override
  public long sum() {
    return mapper.sum();
  }

  @Override
  public void addDerivedStatistic(final ChainedOperationObserver<? super D> derived) {
    mapper.addDerivedStatistic(derived);
  }

  @Override
  public void removeDerivedStatistic(ChainedOperationObserver<? super D> derived) {
    mapper.removeDerivedStatistic(derived);
  }

  @Override
  public void begin() {
    mapper.begin();
  }

  @Override
  public void end(D result) {
    mapper.end(result);
  }

  @SuppressWarnings("unchecked")
  private static <S extends Enum<S>> OperationStatistic<S> findOperationStat(Object rootNode, final Class<S> statisticType, final String statName) {
    Query q = queryBuilder().descendants()
        .filter(context(identifier(subclassOf(OperationStatistic.class))))
        .filter(context(attributes(Matchers.allOf(
            hasAttribute("name", statName),
            hasAttribute("this", new Matcher<OperationStatistic<?>>() {
              @Override
              protected boolean matchesSafely(OperationStatistic<?> object) {
                return object.type().equals(statisticType);
              }
            })
        )))).build();


    Set<TreeNode> result = q.execute(Collections.singleton(ContextManager.nodeFor(rootNode)));

    if (result.size() != 1) {
      throw new RuntimeException("a single stat was expected; found " + result.size());
    }

    TreeNode node = result.iterator().next();
    return (OperationStatistic<S>) node.getContext().attributes().get("this");
  }

}

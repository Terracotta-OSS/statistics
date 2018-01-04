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
package org.terracotta.statistics.registry;

import org.terracotta.context.ContextManager;
import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Matcher;
import org.terracotta.context.query.Matchers;
import org.terracotta.statistics.OperationStatistic;
import org.terracotta.statistics.Sample;
import org.terracotta.statistics.SampledStatistic;
import org.terracotta.statistics.StatisticType;
import org.terracotta.statistics.Table;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.ValueStatistic;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.terracotta.context.query.Matchers.attributes;
import static org.terracotta.context.query.Matchers.context;
import static org.terracotta.context.query.Matchers.hasAttribute;
import static org.terracotta.context.query.Matchers.identifier;
import static org.terracotta.context.query.Matchers.subclassOf;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;
import static org.terracotta.statistics.SampledStatisticAdapter.sample;
import static org.terracotta.statistics.SuppliedValueStatistic.counter;
import static org.terracotta.statistics.SuppliedValueStatistic.gauge;
import static org.terracotta.statistics.SuppliedValueStatistic.supply;
import static org.terracotta.statistics.SuppliedValueStatistic.table;

/**
 * This class replaces the previous {@link StatisticRegistry}
 * in the cases where you do not need any sampling and history.
 * <p>
 * This class typically does a sort of mapping between the registrations and the discovered
 * operations or passthrough statistics.
 * <p>
 * This class also support the generation of management metadata from the discovered statistics.
 * <p>
 * Non thread-safe.
 *
 * @author Mathieu Carbou
 */
public class StatisticRegistry {

  private final Object contextObject;
  private final Supplier<Long> timeSource;
  private final Map<String, SampledStatistic<? extends Serializable>> statistics = new HashMap<>();

  public StatisticRegistry(Object contextObject) {
    this(contextObject, Time::absoluteTime);
  }

  public StatisticRegistry(Object contextObject, Supplier<Long> timeSource) {
    this.contextObject = contextObject;
    this.timeSource = Objects.requireNonNull(timeSource);
  }

  public Map<String, SampledStatistic<? extends Serializable>> getStatistics() {
    return statistics;
  }

  /**
   * Query a statistic based on the full statistic name. Returns null if not found.
   */
  public <T extends Serializable> Optional<Statistic<T>> queryStatistic(String fullStatisticName) {
    return queryStatistic(fullStatisticName, 0);
  }

  /**
   * Query a statistic based on the full statistic name. Returns null if not found.
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> Optional<Statistic<T>> queryStatistic(String fullStatisticName, long sinceMillis) {
    return Optional.ofNullable((SampledStatistic<T>) statistics.get(fullStatisticName))
        .map(statistic -> new Statistic<>(statistic.type(), filter(statistic.type(), statistic.history(sinceMillis))));
  }

  public Map<String, Statistic<? extends Serializable>> queryStatistics() {
    return queryStatistics(0);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Statistic<? extends Serializable>> queryStatistics(long sinceMillis) {
    Map<String, Statistic<? extends Serializable>> stats = new HashMap<>(statistics.size());
    statistics.forEach((name, stat) -> stats.put(name, new Statistic<>(stat.type(), filter(stat.type(), stat.history(sinceMillis)))));
    return stats;
  }

  public <T extends Serializable> void registerStatistic(String fullStatName, StatisticType type, Supplier<T> accessor) {
    registerStatistic(fullStatName, supply(type, accessor));
  }

  public <T extends Serializable> void registerStatistic(String fullStatName, ValueStatistic<T> accessor) {
    registerSampledStatistic(fullStatName, sample(accessor, timeSource));
  }

  public <T extends Serializable> void registerSampledStatistic(String fullStatName, SampledStatistic<T> accessor) {
    if (statistics.put(fullStatName, accessor) != null) {
      throw new IllegalArgumentException("Found duplicate statistic " + fullStatName);
    }
  }

  /**
   * Directly register a TABLE stat with its accessors
   */
  public void registerTable(String fullStatName, Supplier<Table> accessor) {
    registerStatistic(fullStatName, table(accessor));
  }

  /**
   * Directly register a GAUGE stat with its accessor
   */
  public void registerGauge(String fullStatName, Supplier<Number> accessor) {
    registerStatistic(fullStatName, gauge(accessor));
  }

  /**
   * Directly register a COUNTER stat with its accessor
   */
  public void registerCounter(String fullStatName, Supplier<Number> accessor) {
    registerStatistic(fullStatName, counter(accessor));
  }

  public <T extends Serializable> boolean registerStatistic(String statNameSuffix, ValueStatisticDescriptor descriptor) {
    // ignore registering through descriptors if we do not have a context object to find in the tree
    if (contextObject == null) {
      return false;
    }
    TreeNode treeNode = ContextManager.nodeFor(contextObject);
    if (treeNode == null) {
      return false;
    }

    Set<TreeNode> result = queryBuilder()
        .descendants()
        .filter(context(attributes(Matchers.allOf(
            hasAttribute("name", descriptor.getObserverName()),
            hasTags(descriptor.getTags())))))
        .filter(context(identifier(subclassOf(ValueStatistic.class))))
        .build().execute(Collections.singleton(treeNode));

    if (!result.isEmpty()) {
      for (TreeNode node : result) {
        String discriminator = null;

        Map<String, Object> properties = (Map<String, Object>) node.getContext().attributes().get("properties");
        if (properties != null && properties.containsKey("discriminator")) {
          discriminator = properties.get("discriminator").toString();
        }

        String fullStatName = (discriminator == null ? "" : (discriminator + ":")) + statNameSuffix;
        ValueStatistic<T> statistic = (ValueStatistic<T>) node.getContext().attributes().get("this");

        registerStatistic(fullStatName, statistic);
      }
      return true;
    } else {
      return false;
    }
  }

  public <T extends Enum<T>> boolean registerStatistic(String statNameSuffix, final OperationStatisticDescriptor<T> descriptor, final EnumSet<T> outcomes) {
    // ignore registering through descriptors if we do not have a context object to find in the tree
    if (contextObject == null) {
      return false;
    }
    TreeNode treeNode = ContextManager.nodeFor(contextObject);
    if (treeNode == null) {
      return false;
    }

    Set<TreeNode> result = queryBuilder()
        .descendants()
        .filter(context(attributes(Matchers.allOf(
            hasAttribute("type", descriptor.getType()),
            hasAttribute("name", descriptor.getObserverName()),
            hasTags(descriptor.getTags())))))
        .filter(context(identifier(subclassOf(OperationStatistic.class))))
        .build().execute(Collections.singleton(treeNode));

    if (!result.isEmpty()) {
      for (TreeNode node : result) {
        String discriminator = null;

        Map<String, Object> properties = (Map<String, Object>) node.getContext().attributes().get("properties");
        if (properties != null && properties.containsKey("discriminator")) {
          discriminator = properties.get("discriminator").toString();
        }

        String fullStatName = (discriminator == null ? "" : (discriminator + ":")) + statNameSuffix;
        final OperationStatistic<T> statistic = (OperationStatistic<T>) node.getContext().attributes().get("this");

        registerStatistic(fullStatName, statistic.statistic(outcomes));
      }
      return true;
    } else {
      return false;
    }
  }

  private Matcher<Map<String, Object>> hasTags(final Collection<String> tags) {
    return hasAttribute("tags", new Matcher<Collection<String>>() {
      @Override
      protected boolean matchesSafely(Collection<String> object) {
        return object.containsAll(tags);
      }
    });
  }

  private <T extends Serializable> List<Sample<T>> filter(StatisticType type, List<Sample<T>> history) {
    return history.stream()
        // we generally not accept null values for statistics - it means that is not available right now
        .filter(s -> s.getSample() != null)
        .collect(toList());
  }

}

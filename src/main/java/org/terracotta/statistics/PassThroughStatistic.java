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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.terracotta.context.WeakIdentityHashMap;
import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.statistics.extended.StatisticType;

@ContextAttribute(value="this")
class PassThroughStatistic<T extends Serializable> implements ValueStatistic<T> {

  private static final WeakIdentityHashMap<Object, Collection<PassThroughStatistic<? extends Serializable>>> BINDING = new WeakIdentityHashMap<>();
  
  private static void bindStatistic(PassThroughStatistic<? extends Serializable> stat, Object to) {
    Collection<PassThroughStatistic<? extends Serializable>> collection = BINDING.get(to);
    if (collection == null) {
      collection = new CopyOnWriteArrayList<>();
      Collection<PassThroughStatistic<? extends Serializable>> racer = BINDING.putIfAbsent(to, collection);
      if (racer != null) {
        collection = racer;
      }
    }
    collection.add(stat);
  }

  public static void removeStatistics(Object to) {
    BINDING.remove(to);
  }

  static boolean hasStatisticsFor(Object to) {
    return BINDING.get(to) != null;
  }

  @ContextAttribute("name") public final String name;
  @ContextAttribute("tags") public final Set<String> tags;
  @ContextAttribute("properties") public final Map<String, Object> properties;
  @ContextAttribute("type") public final StatisticType type;
  private final ValueStatistic<T> source;

  public PassThroughStatistic(Object context, String name, Set<String> tags, Map<String, ? extends Object> properties, ValueStatistic<T> source) {
    this.name = name;
    this.tags = Collections.unmodifiableSet(new HashSet<>(tags));
    this.properties = Collections.unmodifiableMap(new HashMap<String, Object>(properties));
    this.source = source;
    this.type = source.type();
    bindStatistic(this, context);
  }

  @Override
  public T value() {
    return source.value();
  }

  @Override
  public StatisticType type() {
    return type;
  }
}

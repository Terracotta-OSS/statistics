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


import org.terracotta.context.ContextElement;
import org.terracotta.context.ContextManager;
import org.terracotta.context.TreeNode;
import org.terracotta.statistics.extended.StatisticType;
import org.terracotta.statistics.observer.OperationObserver;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.terracotta.statistics.SuppliedValueStatistic.supply;

public class StatisticsManager extends ContextManager {

  private static final String EQ = Pattern.quote("=");

  static {
    ContextManager.registerContextCreationListener(StatisticsManager::parseStatisticAnnotations);
  }

  public static <T extends Enum<T>> OperationObserver<T> createOperationStatistic(Object context, String name, Set<String> tags, Class<T> eventTypes) {
    return createOperationStatistic(context, name, tags, Collections.emptyMap(), eventTypes);
  }

  public static <T extends Enum<T>> OperationObserver<T> createOperationStatistic(Object context, String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> resultType) {
    OperationStatistic<T> stat = createOperationStatistic(name, tags, properties, resultType);
    associate(context).withChild(stat);
    return stat;
  }

  private static <T extends Enum<T>> OperationStatistic<T> createOperationStatistic(String name, Set<String> tags, Map<String, ? extends Object> properties, Class<T> resultType) {
    return new GeneralOperationStatistic<>(name, tags, properties, resultType);
  }

  public static <T extends Enum<T>> OperationStatistic<T> getOperationStatisticFor(OperationObserver<T> observer) {
    TreeNode node = ContextManager.nodeFor(observer);
    if (node == null) {
      return null;
    } else {
      ContextElement context = node.getContext();
      if (OperationStatistic.class.isAssignableFrom(context.identifier())) {
        @SuppressWarnings("unchecked")
        OperationStatistic<T> stat = (OperationStatistic<T>) context.attributes().get("this");
        return stat;
      } else {
        throw new AssertionError();
      }
    }
  }

  public static <T extends Serializable> void createPassThroughStatistic(Object context, String name, Set<String> tags, StatisticType type, Supplier<T> source) {
    createPassThroughStatistic(context, name, tags, Collections.emptyMap(), supply(type, source));
  }

  public static <T extends Serializable> void createPassThroughStatistic(Object context, String name, Set<String> tags, ValueStatistic<T> source) {
    createPassThroughStatistic(context, name, tags, Collections.emptyMap(), source);
  }

  public static <T extends Serializable> void createPassThroughStatistic(Object context, String name, Set<String> tags, Map<String, ? extends Object> properties, StatisticType type, Supplier<T> source) {
    createPassThroughStatistic(context, name, tags, properties, supply(type, source));
  }

  public static <T extends Serializable> void createPassThroughStatistic(Object context, String name, Set<String> tags, Map<String, ? extends Object> properties, ValueStatistic<T> source) {
    PassThroughStatistic<T> stat = new PassThroughStatistic<>(context, name, tags, properties, source);
    associate(context).withChild(stat);
  }

  public static void removePassThroughStatistics(Object context) {
    PassThroughStatistic.removeStatistics(context);
  }

  public static Set<String> tags(String... tags) {return new HashSet<>(Arrays.asList(tags));}

  private static void parseStatisticAnnotations(final Object object) {
    for (final Method m : object.getClass().getMethods()) {
      Statistic anno = m.getAnnotation(Statistic.class);
      if (anno != null) {
        Class<?> returnType = m.getReturnType();
        if (m.getParameterTypes().length != 0) {
          throw new IllegalArgumentException("Statistic methods must be no-arg: " + m);
        } else if (!Number.class.isAssignableFrom(returnType) && (!m.getReturnType().isPrimitive() || m.getReturnType().equals(Boolean.TYPE))) {
          throw new IllegalArgumentException("Statistic methods must return a Number: " + m);
        } else if (Modifier.isStatic(m.getModifiers())) {
          throw new IllegalArgumentException("Statistic methods must be non-static: " + m);
        } else {
          StatisticsManager.createPassThroughStatistic(object, anno.name(), new HashSet<>(Arrays.asList(anno.tags())), supply(anno.type(), new ReflectionSupplier<>(object, m)));
        }
      }
    }
  }

  public static Map<String, String> properties(String... kvs) {
    return Stream.of(kvs)
        .map(kv -> kv.split(EQ, 2))
        .collect(Collectors.toMap(split -> split[0], split -> split[1]));
  }

  static class ReflectionSupplier<T> implements Supplier<T> {

    private final WeakReference<Object> targetRef;
    private final Method method;

    ReflectionSupplier(Object target, Method method) {
      this.targetRef = new WeakReference<>(target);
      this.method = method;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
      try {
        return (T) method.invoke(targetRef.get());
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e.getTargetException());
      }
    }
  }
}

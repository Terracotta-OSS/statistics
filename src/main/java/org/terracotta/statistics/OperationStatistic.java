/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.statistics.jsr166e.LongAdder;
import org.terracotta.statistics.observer.OperationObserver;

@ContextAttribute("this")
public class OperationStatistic<T extends Enum<T>> extends AbstractSourceStatistic<OperationObserver<? super T>> implements OperationObserver<T> {

  private final Map<String, Object> properties;
  private final Class<T> type;
  private final EnumMap<T, LongAdder> counts;
  
  public OperationStatistic(Map<String, ? extends Object> properties, Class<T> type) {
    this.properties = Collections.unmodifiableMap(new HashMap<String, Object>(properties));
    this.type = type;
    this.counts = new EnumMap<T, LongAdder>(type);
    for (T t : type.getEnumConstants()) {
      counts.put(t, new LongAdder());
    }
  }
  
  public ValueStatistic<Long> statistic(T result) {
    final LongAdder adder = counts.get(result);
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return adder.sum();
      }
    };
  }
  
  @ContextAttribute("properties")
  public Map<String, Object> properties() {
    return properties;
  }
  
  @ContextAttribute("type")
  public Class<T> type() {
    return type;
  }
  
  public long count(T type) {
    return counts.get(type).sum();
  }

  @Override
  public void begin() {
    for (OperationObserver<? super T> observer : derivedStatistics) {
      observer.begin();
    }
  }

  @Override
  public void end(T result) {
    counts.get(result).increment();
    for (OperationObserver<? super T> observer : derivedStatistics) {
      observer.end(result);
    }
  }

  @Override
  public void end(T result, long parameter) {
    counts.get(result).increment();
    for (OperationObserver<? super T> observer : derivedStatistics) {
      observer.end(result, parameter);
    }
  }
  
  @Override
  public String toString() {
    return counts.toString();
  }
}

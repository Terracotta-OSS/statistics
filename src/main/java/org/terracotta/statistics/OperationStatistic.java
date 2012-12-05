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

/**
 * An operation observer that tracks operation result counts and can drive further derived statistics.
 * 
 * @param <T> the operation result enum type
 */
@ContextAttribute("this")
public class OperationStatistic<T extends Enum<T>> extends AbstractSourceStatistic<OperationObserver<? super T>> implements OperationObserver<T> {

  private final Map<String, Object> properties;
  private final Class<T> type;
  private final EnumMap<T, LongAdder> counts;
  
  /**
   * Create an operation statistics for a given operation result type.
   * 
   * @param properties a set of context properties
   * @param type operation result type
   */
  public OperationStatistic(Map<String, ? extends Object> properties, Class<T> type) {
    this.properties = Collections.unmodifiableMap(new HashMap<String, Object>(properties));
    this.type = type;
    this.counts = new EnumMap<T, LongAdder>(type);
    for (T t : type.getEnumConstants()) {
      counts.put(t, new LongAdder());
    }
  }
  
  /**
   * Return a {@ValueStatistic<Long>} returning the count for the given result.
   * 
   * @param result the result of interest 
   * @return a {@code ValueStatistic} instance
   */
  public ValueStatistic<Long> statistic(T result) {
    final LongAdder adder = counts.get(result);
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return adder.sum();
      }
    };
  }
  
  /**
   * Return the properties of this statistic.
   * <p>
   * This method is annotated with {@code ContextAttribute} so these properties
   * are extracted to an associated {@code ContextElement}.
   * 
   * @return the statistics properties
   */
  @ContextAttribute("properties")
  public Map<String, Object> properties() {
    return properties;
  }
  
  /**
   * Return the result type of this operation.
   * 
   * @return the result type
   */
  @ContextAttribute("type")
  public Class<T> type() {
    return type;
  }
  
  /**
   * Return the count of operations with the given type.
   * 
   * @param type the result type
   * @return the operation count
   */
  public long count(T type) {
    return counts.get(type).sum();
  }

  @Override
  public void begin() {
    for (OperationObserver<? super T> observer : derived()) {
      observer.begin();
    }
  }

  @Override
  public void end(T result) {
    counts.get(result).increment();
    for (OperationObserver<? super T> observer : derived()) {
      observer.end(result);
    }
  }
  
  @Override
  public void end(T result, long ... parameters) {
    counts.get(result).increment();
    for (OperationObserver<? super T> observer : derived()) {
      observer.end(result, parameters);
    }
  }
  
  @Override
  public String toString() {
    return counts.toString();
  }
}

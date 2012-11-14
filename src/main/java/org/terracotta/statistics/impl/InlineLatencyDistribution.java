/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.terracotta.statistics.Distribution;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.observer.OperationObserver;

public class InlineLatencyDistribution<T extends Enum<T>> extends AbstractSourceStatistic<EventObserver> implements Distribution<Long>, OperationObserver<T> {

  private final AtomicLong minimum = new AtomicLong(Long.MAX_VALUE);
  private final AtomicLong maximum = new AtomicLong(Long.MIN_VALUE);

  private final AtomicLong sum = new AtomicLong();
  private final AtomicLong count = new AtomicLong();
  
  private final ThreadLocal<Long> eventStart = new ThreadLocal<Long>();

  private final T eventType;
  
  public InlineLatencyDistribution(T observe) {
    eventType = observe;
  }
  
  @Override
  public Long minimum() {
    return minimum.get();
  }

  @Override
  public Float mean() {
    return ((float) sum.get()) / count.get();
  }

  @Override
  public Long maximum() {
    return maximum.get();
  }

  @Override
  public void begin() {
    eventStart.set(System.nanoTime());
  }

  @Override
  public void end(T result) {
    if (eventType.equals(result)) {
      Long start = eventStart.get();
      if (start != null) {
        event(System.nanoTime() - start);
      }
    }
    eventStart.remove();
  }

  @Override
  public void end(T result, long parameter) {
    end(result);
  }
  
  private void event(long latency) {
    for (long min = minimum.get(); min > latency && !minimum.compareAndSet(min, latency); min = minimum.get());
    for (long max = maximum.get(); max < latency && !maximum.compareAndSet(max, latency); max = maximum.get());
    sum.addAndGet(latency);
    count.incrementAndGet();
    
    for (EventObserver observer : derivedStatistics) {
      observer.event(latency);
    }
  }
}

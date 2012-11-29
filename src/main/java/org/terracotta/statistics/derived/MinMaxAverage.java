/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.derived;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.util.InThreadExecutor;

import static java.lang.Float.floatToIntBits;
import static java.lang.Float.intBitsToFloat;

/**
 *
 * @author cdennis
 */
public class MinMaxAverage implements EventObserver {

  private final AtomicLong maximum = new AtomicLong(Long.MIN_VALUE);
  private final AtomicLong minimum = new AtomicLong(Long.MAX_VALUE);
  
  private final AtomicInteger summation = new AtomicInteger(floatToIntBits(0.0f));
  private final AtomicLong count = new AtomicLong(0);

  private final Executor executor;
  
  public MinMaxAverage() {
    this(InThreadExecutor.INSTANCE);
  }
  
  public MinMaxAverage(Executor executor) {
    this.executor = executor;
  }
  
  
  @Override
  public void event(final long parameter) {
    executor.execute(new Runnable() {

      @Override
      public void run() {
        for (long max = maximum.get(); max < parameter && !maximum.compareAndSet(max, parameter); max = maximum.get());
        for (long min = minimum.get(); min > parameter && !minimum.compareAndSet(min, parameter); min = minimum.get());
        for (int sumBits = summation.get(); !summation.compareAndSet(sumBits, floatToIntBits(intBitsToFloat(sumBits) + parameter)); sumBits = summation.get());
        count.incrementAndGet();
      }
    });
  }

  public Long min() {
    if (count.get() == 0) {
      return null;
    } else {
      return minimum.get();
    }
  }
  
  public Float mean() {
    if (count.get() == 0) {
      return null;
    } else {
      return intBitsToFloat(summation.get()) / count.get();
    }
  }
  
  public Long max() {
    if (count.get() == 0) {
      return null;
    } else {
      return maximum.get();
    }
  }
}

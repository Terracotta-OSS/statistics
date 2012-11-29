/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.derived;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.util.InThreadExecutor;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

/**
 *
 * @author cdennis
 */
public class MinMaxAverage implements EventObserver {

  private final AtomicLong maximum = new AtomicLong(Long.MIN_VALUE);
  private final AtomicLong minimum = new AtomicLong(Long.MAX_VALUE);
  
  private final AtomicLong summation = new AtomicLong(doubleToLongBits(0.0));
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
        for (long sumBits = summation.get(); !summation.compareAndSet(sumBits, doubleToLongBits(longBitsToDouble(sumBits) + parameter)); sumBits = summation.get());
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
  
  public ValueStatistic<Long> minStatistic() {
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return min();
      }
    };
  }
  
  public Double mean() {
    if (count.get() == 0) {
      return null;
    } else {
      return longBitsToDouble(summation.get()) / count.get();
    }
  }
  
  public ValueStatistic<Double> meanStatistic() {
    return new ValueStatistic<Double>() {

      @Override
      public Double value() {
        return mean();
      }
    };
  }
  
  public Long max() {
    if (count.get() == 0) {
      return null;
    } else {
      return maximum.get();
    }
  }
  
  public ValueStatistic<Long> maxStatistic() {
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return max();
      }
    };
  }
}

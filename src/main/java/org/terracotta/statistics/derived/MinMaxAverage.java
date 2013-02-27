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
package org.terracotta.statistics.derived;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.observer.ChainedEventObserver;
import org.terracotta.statistics.util.InThreadExecutor;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

/**
 *
 * @author cdennis
 */
public class MinMaxAverage implements ChainedEventObserver {

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
  public void event(long time, final long ... parameters) {
    executor.execute(new Runnable() {

      @Override
      public void run() {
        for (long max = maximum.get(); max < parameters[0] && !maximum.compareAndSet(max, parameters[0]); max = maximum.get());
        for (long min = minimum.get(); min > parameters[0] && !minimum.compareAndSet(min, parameters[0]); min = minimum.get());
        for (long sumBits = summation.get(); !summation.compareAndSet(sumBits, doubleToLongBits(longBitsToDouble(sumBits) + parameters[0])); sumBits = summation.get());
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

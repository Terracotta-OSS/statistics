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

import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.observer.ChainedEventObserver;
import org.terracotta.statistics.util.InThreadExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;

import static org.terracotta.statistics.ValueStatistics.gauge;

/**
 * @author cdennis
 */
public class MinMaxAverage implements ChainedEventObserver {

  private final LongAccumulator maximum = new LongAccumulator(Math::max, Long.MIN_VALUE);
  private final LongAccumulator minimum = new LongAccumulator(Math::min, Long.MAX_VALUE);

  private final DoubleAdder summation = new DoubleAdder();
  private final AtomicLong count = new AtomicLong(0);

  private final Executor executor;

  public MinMaxAverage() {
    this(InThreadExecutor.INSTANCE);
  }

  public MinMaxAverage(Executor executor) {
    this.executor = executor;
  }

  @Override
  public void event(long time, final long... parameters) {
    executor.execute(() -> {
      long value = parameters[0];
      maximum.accumulate(value);
      minimum.accumulate(value);
      summation.add(value);
      count.incrementAndGet();
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
    return gauge(this::min);
  }

  public Double mean() {
    if (count.get() == 0) {
      return null;
    } else {
      return summation.sum() / count.get();
    }
  }

  public ValueStatistic<Double> meanStatistic() {
    return gauge(this::mean);
  }

  public Long max() {
    if (count.get() == 0) {
      return null;
    } else {
      return maximum.get();
    }
  }

  public ValueStatistic<Long> maxStatistic() {
    return gauge(this::max);
  }
}

/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.statistics.derived.latency;

import org.terracotta.statistics.observer.ChainedEventObserver;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * This accumulator accumulate latency metrics. It is lock-free so when computing
 * some values,it is possible that a thread is writing some values while
 * the snapshot is being computed. So there is the possibility of an error in the
 * result.
 *
 * @author Mathieu Carbou
 */
public class LatencyAccumulator implements LatencyStatistic, ChainedEventObserver {

  private static final long DEFAULT_MIN = Long.MAX_VALUE;
  private static final long DEFAULT_MAX = Long.MIN_VALUE;

  private final LongAdder count = new LongAdder();
  private final LongAdder total = new LongAdder();
  private final LongAccumulator minimum = new LongAccumulator(Math::min, DEFAULT_MIN);
  private final LongAccumulator maximum = new LongAccumulator(Math::max, DEFAULT_MAX);

  private LatencyAccumulator(long... latencies) {
    for (long latency : latencies) {
      accumulate(latency);
    }
  }

  public void accumulate(long latency) {
    count.increment();
    total.add(latency);
    minimum.accumulate(latency);
    maximum.accumulate(latency);
  }

  public void accumulate(LatencyAccumulator accumulator) {
    count.add(accumulator.count());
    total.add(accumulator.total());
    minimum.accumulate(accumulator.minimum());
    maximum.accumulate(accumulator.maximum());
  }

  public long count() {
    return count.sum();
  }

  public long total() {
    return total.sum();
  }

  public boolean isEmpty() {
    return count.sum() == 0;
  }

  @Override
  public void event(long time, long latency) {
    accumulate(latency);
  }

  @Override
  public Long maximum() {
    return isEmpty() ? null : maximum.get();
  }

  @Override
  public Long minimum() {
    return isEmpty() ? null : minimum.get();
  }

  @Override
  public double average() {
    return ((double) total.sum()) / count.sum();
  }

  @Override
  public String toString() {
    return "LatencyAccumulator{" +
        "count=" + count() +
        ", total=" + total() +
        ", minimum=" + minimum() +
        ", maximum=" + maximum() +
        ", average=" + average() +
        '}';
  }

  public static LatencyAccumulator accumulator(long... latencies) {
    return new LatencyAccumulator(latencies);
  }

  public static LatencyAccumulator empty() {
    return new LatencyAccumulator();
  }

}

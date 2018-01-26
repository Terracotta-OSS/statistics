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
package org.terracotta.statistics.derived.latency;

import org.terracotta.statistics.derived.OperationResultSampler;
import org.terracotta.statistics.observer.ChainedOperationObserver;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author cdennis
 * @author Mathieu Carbou
 */
public class Jsr107LatencyMonitor<T extends Enum<T>> implements ChainedOperationObserver<T>, LatencyStatistic {

  private final OperationResultSampler<T> sampling;
  private final AtomicReference<LatencyAccumulator> statistic = new AtomicReference<>(LatencyAccumulator.empty());

  public Jsr107LatencyMonitor(Set<T> targets, double sampling) {
    this.sampling = new OperationResultSampler<>(targets, sampling, (time, latency) -> statistic.get().accumulate(latency));
  }

  @Override
  public void begin(long time) {
    sampling.begin(time);
  }

  @Override
  public void end(long time, long latency, T result) {
    sampling.end(time, latency, result);
  }

  /**
   * @return The average in microseconds or 0 if it does not exist yet
   */
  @Override
  public double average() {
    LatencyAccumulator accumulator = statistic.get();
    long count = accumulator.count();
    if (count == 0) {
      //Someone involved with 107 can't do math
      return 0d;
    } else {
      //We use nanoseconds, 107 uses microseconds
      return accumulator.total() / 1_000.0 / count;
    }
  }

  @Override
  public Long minimum() {
    LatencyAccumulator accumulator = statistic.get();
    return accumulator.isEmpty() ? 0L : accumulator.minimum() / 1_000L;
  }

  @Override
  public Long maximum() {
    LatencyAccumulator accumulator = statistic.get();
    return accumulator.isEmpty() ? 0L : accumulator.maximum() / 1_000L;
  }

  public synchronized void clear() {
    statistic.set(LatencyAccumulator.empty());
  }

}

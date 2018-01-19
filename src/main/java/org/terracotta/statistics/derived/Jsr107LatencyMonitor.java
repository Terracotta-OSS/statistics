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

import org.terracotta.statistics.observer.ChainedOperationObserver;

import java.util.Set;

/**
 * @author cdennis
 */
public class Jsr107LatencyMonitor<T extends Enum<T>> implements ChainedOperationObserver<T>, LatencyStatistic {

  private final OperationResultSampler<T> sampling;
  private volatile LatencyMinMaxAverage statistic;

  public Jsr107LatencyMonitor(Set<T> targets) {
    this.statistic = new LatencyMinMaxAverage();
    this.sampling = new OperationResultSampler<>(targets, 1.0, statistic);
  }

  @Override
  public void begin(long time) {
    sampling.begin(time);
  }

  @Override
  public void end(long time, long latency, T result) {
    sampling.end(time, latency, result);
  }

  @Override
  public Long minimum() {return statistic.minimum();}

  @Override
  public Long maximum() {return statistic.maximum();}

  /**
   * @return The average in microseconds or 0 if it does not exist yet
   */
  @Override
  public Double average() {
    return jsr107Average();
  }

  /**
   * @return The average in microseconds or 0 if it does not exist yet
   */
  public double jsr107Average() {
    Double value = statistic.average();
    if (value == null) {
      //Someone involved with 107 can't do math
      return 0d;  /**
       * @return The average in microseconds or 0 if it does not exist yet
       */
    } else {
      //We use nanoseconds, 107 uses microseconds
      return value / 1000f;
    }
  }

  public synchronized void clear() {
    sampling.removeDerivedStatistic(statistic);
    statistic = new LatencyMinMaxAverage();
    sampling.addDerivedStatistic(statistic);
  }

}
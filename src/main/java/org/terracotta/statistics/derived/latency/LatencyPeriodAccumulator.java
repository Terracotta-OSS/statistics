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

/**
 * @author Mathieu Carbou
 */
public class LatencyPeriodAccumulator implements LatencyStatistic, ChainedEventObserver {

  private final LatencyAccumulator accumulator;
  private final long start;
  private final long end;

  LatencyPeriodAccumulator(long start, long length, long... latencies) {
    this.start = start;
    this.end = start + length;
    accumulator = LatencyAccumulator.accumulator(latencies);
  }

  public boolean isBefore(long time) {
    return end <= time;
  }

  public boolean isAfter(long time) {
    return start > time;
  }

  /**
   * @return Start time (inclusive) for this period.
   */
  public long start() {
    return start;
  }

  /**
   * @return End time (exclusive) for this period.
   */
  public long end() {
    return end;
  }

  public LatencyAccumulator accumulator() {
    return accumulator;
  }

  public boolean tryAccumulate(long time, long latency) {
    // do not compare by start (time >= start && ...)
    // because if 2 threads are coming and the first one 
    // gets de-schedulded and the second one creates a new accumulator,
    // then we will still be able to accept the value of the first 
    // thread by just comparing with end. The new accumulator will then
    // accept a value that should have been accumulated instead in the 
    // previous accumulator.
    if (time < end) {
      accumulator.accumulate(latency);
      return true;
    }
    return false;
  }

  @Override
  public void event(long timeNs, long latencyNs) {
    tryAccumulate(timeNs, latencyNs);
  }

  @Override
  public Long minimum() {
    return accumulator.minimum();
  }

  @Override
  public Long maximum() {
    return accumulator.maximum();
  }

  @Override
  public double average() {
    return accumulator.average();
  }

  @Override
  public String toString() {
    return "LatencyPeriodAccumulator{" +
        "start=" + start +
        ", end=" + end +
        ", accumulator=" + accumulator +
        '}';
  }
}

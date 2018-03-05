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

import org.terracotta.statistics.Sample;
import org.terracotta.statistics.SampledStatistic;
import org.terracotta.statistics.StatisticType;
import org.terracotta.statistics.Time;
import org.terracotta.statistics.observer.ChainedEventObserver;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

/**
 * Keeps a history of the maximum operation latencies over a specific window.
 * The window needs to be as small as possible to get the most detailed history.
 * <p>
 * This is important to note that the history is based on a sample count and not a time frame.
 * If you set a history to 100 samples and a window of 500ms, you won't have a complete
 * time-frame of 10 seconds. You can have a sample at t0, then another one at t0 + 1 sec depending
 * on when the operations occur.
 *
 * @author Mathieu Carbou
 */
public class MaximumLatencyHistory implements ChainedEventObserver, SampledStatistic<Long> {

  private final AtomicReference<LatencyPeriodAccumulator> latestAccumulator = new AtomicReference<>();
  private final Queue<LatencyPeriodAccumulator> archive;
  private final long windowSizeNs;
  private final Consumer<LatencyPeriodAccumulator> sink;
  private final LongSupplier timeSupplier;
  private volatile long drift;

  public MaximumLatencyHistory(int historySize, long windowSize, TimeUnit windowSizeUnit, LongSupplier timeSupplier) {
    this(historySize, windowSize, windowSizeUnit, timeSupplier, accumulator -> {});
  }

  /**
   * @param historySize    The number of samples to keep
   * @param windowSize     The size of the window over which the reduction is applied. A small value is better for more details, but history might discard values faster.
   * @param windowSizeUnit Window size unit
   * @param sink           The sink used to collect the old values that are discarded from the history.
   */
  public MaximumLatencyHistory(int historySize, long windowSize, TimeUnit windowSizeUnit, LongSupplier timeSupplier, Consumer<LatencyPeriodAccumulator> sink) {
    this.archive = new ArrayBlockingQueue<>(historySize);
    this.windowSizeNs = TimeUnit.NANOSECONDS.convert(windowSize, windowSizeUnit);
    this.sink = sink;
    this.timeSupplier = timeSupplier;
    this.drift = Time.time() - timeSupplier.getAsLong() * 1_000_000;
  }

  @Override
  public void event(long timeNs, long latencyNs) {
    while (true) {
      LatencyPeriodAccumulator accumulator = latestAccumulator.get();
      if (accumulator != null && accumulator.tryAccumulate(timeNs, latencyNs)) {
        return;
      }
      LatencyPeriodAccumulator newAccumulator = new LatencyPeriodAccumulator(timeNs, windowSizeNs, latencyNs);
      if (latestAccumulator.compareAndSet(accumulator, newAccumulator)) {
        // The difference between system time and nano time needs to be recomputed
        // in case the computer went to sleep. In this case, the system time advance but not the nano time.
        this.drift = Time.time() - timeSupplier.getAsLong() * 1_000_000;
        // Insertion will in theory be in order because for whole duration of the new window, 
        // there cannot be another thread that will try to insert at the same time.
        insert(newAccumulator);
        return;
      }
    }
  }

  @Override
  public Long value() {
    LatencyPeriodAccumulator accumulator = latestAccumulator.get();
    if (accumulator == null || accumulator.end() <= Time.time()) {
      return null;
    }
    return accumulator.maximum();
  }

  @Override
  public StatisticType type() {
    return StatisticType.GAUGE;
  }

  @Override
  public List<Sample<Long>> history() {
    long drift = this.drift;
    return archive.stream()
        .map(acumulator -> {
          return new Sample<>((acumulator.start() - drift) / 1_000_000, acumulator.maximum());
        })
        .collect(Collectors.toList());
  }

  @Override
  public List<Sample<Long>> history(long sinceMillis) {
    long drift = this.drift;
    long sinceNs = sinceMillis * 1_000_000 + drift;
    return archive.stream()
        .filter(acumulator -> acumulator.start() >= sinceNs)
        .map(acumulator -> new Sample<>((acumulator.start() - drift) / 1_000_000, acumulator.maximum()))
        .collect(Collectors.toList());
  }

  private void insert(LatencyPeriodAccumulator newAccumulator) {
    while (!archive.offer(newAccumulator)) {
      LatencyPeriodAccumulator removed = archive.poll();
      if (removed != null) {
        sink.accept(removed);
      }
    }
  }

}

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
package org.terracotta.statistics.archive;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.terracotta.statistics.ValueStatistic;

import static org.terracotta.statistics.Time.absoluteTime;

/**
 *
 * @author cdennis
 */
public class StatisticSampler<T extends Number> {

  private final boolean exclusiveExecutor;
  private final ScheduledExecutorService executor;
  private final Runnable task;
  
  private ScheduledFuture<?> currentExecution;
  private long period;  
  
  public StatisticSampler(long time, TimeUnit unit, ValueStatistic<T> statistic, SampleSink<? super Timestamped<T>> sink) {
    this(null, time, unit, statistic, sink);
  }
  
  public StatisticSampler(ScheduledExecutorService executor, long time, TimeUnit unit, ValueStatistic<T> statistic, SampleSink<? super Timestamped<T>> sink) {
    if (executor == null) {
      this.exclusiveExecutor = true;
      this.executor = Executors.newSingleThreadScheduledExecutor(new SamplerThreadFactory());
    } else {
      this.exclusiveExecutor = false;
      this.executor = executor;
    }
    this.period = unit.toNanos(time);
    this.task = new SamplingTask(statistic, sink);
  }
  
  public synchronized void setPeriod(long time, TimeUnit unit) {
    this.period = unit.toNanos(time);
    if (currentExecution != null && !currentExecution.isDone()) {
      stop();
      start();
    }
  }

  public synchronized void start() {
    if (currentExecution == null || currentExecution.isDone()) {
      currentExecution = executor.scheduleAtFixedRate(task, period, period, TimeUnit.NANOSECONDS);
    } else {
      throw new IllegalStateException("Sampler is already running");
    }
  }

  public synchronized void stop() {
    if (currentExecution == null || currentExecution.isDone()) {
      throw new IllegalStateException("Sampler is not running");
    } else {
      currentExecution.cancel(false);
    }
  }
  
  public synchronized void shutdown() throws InterruptedException {
    if (exclusiveExecutor) {
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
          throw new IllegalStateException("Exclusive ScheduledExecutorService failed to terminate promptly");
        }
    } else {
      throw new IllegalStateException("ScheduledExecutorService was supplied externally - it must be shutdown directly");
    }
  }
  
  static class SamplingTask<T extends Number> implements Runnable {

    private final ValueStatistic<T> statistic;
    private final SampleSink<Timestamped<T>> sink;
    
    SamplingTask(ValueStatistic<T> statistic, SampleSink<Timestamped<T>> sink) {
      this.statistic = statistic;
      this.sink = sink;
    }
    
    @Override
    public void run() {
      sink.accept(new Sample(absoluteTime(), statistic.value()));
    }
  }
  
  static class Sample<T> implements Timestamped<T> {

    private final T sample;
    private final long timestamp;

    public Sample(long timestamp, T sample) {
      this.sample = sample;
      this.timestamp = timestamp;
    }

    @Override
    public T getSample() {
      return sample;
    }

    @Override
    public long getTimestamp() {
      return timestamp;
    }
    
    @Override
    public String toString() {
      return getSample() + " @ " + new Date(getTimestamp());
    }
  }
  
  static class SamplerThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "Statistic Sampler");
      t.setDaemon(true);
      return t;
    }
  }
}

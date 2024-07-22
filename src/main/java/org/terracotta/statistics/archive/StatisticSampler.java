/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company.
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

import org.terracotta.statistics.Sample;
import org.terracotta.statistics.ValueStatistic;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * @author cdennis
 */
public class StatisticSampler<T extends Serializable> {

  private final boolean exclusiveExecutor;
  private final ScheduledExecutorService executor;
  private final SamplingTask<T> task;

  private ScheduledFuture<?> currentExecution;
  private long period;

  public StatisticSampler(long time, TimeUnit unit, ValueStatistic<T> statistic, Consumer<Sample<T>> sink, LongSupplier timeSupplier) {
    this(null, time, unit, statistic, sink, timeSupplier);
  }

  public StatisticSampler(ScheduledExecutorService executor, long time, TimeUnit unit, ValueStatistic<T> statistic, Consumer<Sample<T>> sink, LongSupplier timeSupplier) {
    if (executor == null) {
      this.exclusiveExecutor = true;
      this.executor = Executors.newSingleThreadScheduledExecutor(new SamplerThreadFactory());
    } else {
      this.exclusiveExecutor = false;
      this.executor = executor;
    }
    this.period = unit.toNanos(time);
    this.task = new SamplingTask<>(statistic, sink, timeSupplier);
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

  static class SamplingTask<T extends Serializable> implements Runnable {

    private final ValueStatistic<T> statistic;
    private final Consumer<Sample<T>> sink;
    private final LongSupplier timeSupplier;

    SamplingTask(ValueStatistic<T> statistic, Consumer<Sample<T>> sink, LongSupplier timeSupplier) {
      this.statistic = statistic;
      this.sink = sink;
      this.timeSupplier = timeSupplier;
    }

    @Override
    public void run() {
      sink.accept(new Sample<>(timeSupplier.getAsLong(), statistic.value()));
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

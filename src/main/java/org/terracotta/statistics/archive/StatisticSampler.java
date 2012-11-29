/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.archive;

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
public class StatisticSampler<T> {

  private final ScheduledExecutorService executor;
  private final long period;
  private final Runnable task;
  
  private ScheduledFuture<?> currentExecution;
  
  public StatisticSampler(long time, TimeUnit unit, ValueStatistic<T> statistic, SampleSink<? super Timestamped<T>> sink) {
    this(Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      }
    }), time, unit, statistic, sink);
  }
  
  public StatisticSampler(ScheduledExecutorService executor, long time, TimeUnit unit, ValueStatistic<T> statistic, SampleSink<? super Timestamped<T>> sink) {
    this.executor = executor;
    this.period = unit.toNanos(time);
    this.task = new SamplingTask(statistic, sink);
  }
  
  public synchronized void start() {
    if (currentExecution == null || currentExecution.isDone()) {
      currentExecution = executor.scheduleAtFixedRate(task, 0, period, TimeUnit.NANOSECONDS);
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
  
  public synchronized void shutdown() {
    executor.shutdown();
  }
  
  static class SamplingTask<T> implements Runnable {

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
  }
}

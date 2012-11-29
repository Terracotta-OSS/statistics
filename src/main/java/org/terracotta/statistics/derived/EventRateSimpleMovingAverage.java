/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.derived;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.jsr166e.LongAdder;
import org.terracotta.statistics.observer.EventObserver;

import static org.terracotta.statistics.Time.time;

/**
 *
 * @author cdennis
 */
public class EventRateSimpleMovingAverage implements EventObserver, ValueStatistic<Double> {

  private static final int PARTITION_COUNT = 10;

  private final long windowSize;
  private final long partitionSize;
  private final Queue<CounterPartition> archive = new ConcurrentLinkedQueue<CounterPartition>();
  private final AtomicReference<CounterPartition> activePartition;
  
  public EventRateSimpleMovingAverage(long time, TimeUnit unit) {
    this.windowSize  = unit.toNanos(time);
    this.partitionSize = windowSize / PARTITION_COUNT;
    this.activePartition = new AtomicReference<CounterPartition>(new CounterPartition(time(), partitionSize));
  }

  @Override
  public Double value() {
    return rateUsingNanos();
  }
  
  public Double rateUsingNanos() {
    long endTime = time();
    long startTime = endTime - windowSize;
    
    CounterPartition current = activePartition.get();
    long count;
    long actualStartTime;
    if (current.isBefore(startTime)) {
      count = 0;
      actualStartTime = startTime;
    } else {
      count = current.sum();
      actualStartTime = current.start();
    }
    for (Iterator<CounterPartition> it = archive.iterator(); it.hasNext(); ) {
      CounterPartition partition = it.next();
      if (partition == current) {
        break;
      } else if (partition.isBefore(startTime)) {
        it.remove();
      } else {
        actualStartTime = Math.min(actualStartTime, partition.start());
        count += partition.sum();
      }
    }
    
    return ((double) count) / (endTime - actualStartTime);
  }
  
  public Double rate(TimeUnit base) {
    return rateUsingNanos() * base.toNanos(1);
  }
  
  @Override
  public void event(long parameter) {
    long time = time();
    while (true) {
      CounterPartition partition = activePartition.get();
      if (partition.targetFor(time)) {
        partition.increment();
        return;
      } else {
        CounterPartition newPartition = new CounterPartition(time, partitionSize);
        if (activePartition.compareAndSet(partition, newPartition)) {
          archive(partition);
          newPartition.increment();
          return;
        }
      }
    }
  }

  private void archive(CounterPartition partition) {
    archive.add(partition);
    
    long startTime = partition.end() - windowSize;
    for (CounterPartition earliest = archive.element(); earliest.isBefore(startTime); earliest = archive.element()) {
      if (archive.remove(earliest)) {
        break;
      }
    }
  }
  
  static class CounterPartition extends LongAdder {

    private final long start;
    private final long end;
    
    CounterPartition(long start, long length) {
      this.start = start;
      this.end = start + length;
    }
    
    public boolean targetFor(long time) {
      return end > time;
    }
    
    public boolean isBefore(long time) {
      return end < time;
    }
    
    public long start() {
      return start;
    }
    
    public long end() {
      return end;
    }
  }
}

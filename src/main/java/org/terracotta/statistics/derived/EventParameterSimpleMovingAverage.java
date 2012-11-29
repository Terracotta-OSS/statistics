/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class EventParameterSimpleMovingAverage implements EventObserver, ValueStatistic<Double> {

  private static final int PARTITION_COUNT = 10;

  private final long windowSize;
  private final long partitionSize;
  private final Queue<AveragePartition> archive = new ConcurrentLinkedQueue<AveragePartition>();
  private final AtomicReference<AveragePartition> activePartition;
  
  public EventParameterSimpleMovingAverage(long time, TimeUnit unit) {
    this.windowSize  = unit.toNanos(time);
    this.partitionSize = windowSize / PARTITION_COUNT;
    this.activePartition = new AtomicReference<AveragePartition>(new AveragePartition(time(), partitionSize));
  }

  public Double value() {
    return average();
  }

  public final double average() {
    long startTime = time() - windowSize;
    
    AveragePartition current = activePartition.get();
    Average average = new Average();
    if (!current.isBefore(startTime)) {
      current.aggregate(average);
    }
    for (Iterator<AveragePartition> it = archive.iterator(); it.hasNext(); ) {
      AveragePartition partition = it.next();
      if (partition == current) {
        break;
      } else if (partition.isBefore(startTime)) {
        it.remove();
      } else {
        partition.aggregate(average);
      }
    }
    
    if (average.count == 0) {
      return Double.NaN;
    } else {
      return ((double) average.total) / average.count;
    }
  }
  
  @Override
  public void event(long parameter) {
    long time = time();
    while (true) {
      AveragePartition partition = activePartition.get();
      if (partition.targetFor(time)) {
        partition.event(parameter);
        return;
      } else {
        AveragePartition newPartition = new AveragePartition(time, partitionSize);
        if (activePartition.compareAndSet(partition, newPartition)) {
          archive(partition);
          newPartition.event(parameter);
          return;
        }
      }
    }
  }

  private void archive(AveragePartition partition) {
    archive.add(partition);
    
    long startTime = partition.end() - windowSize;
    for (AveragePartition earliest = archive.element(); earliest.isBefore(startTime); earliest = archive.element()) {
      if (archive.remove(earliest)) {
        break;
      }
    }
  }
  
  static class AveragePartition {

    private final LongAdder total = new LongAdder();
    private final LongAdder count = new LongAdder();

    private final long start;
    private final long end;
    
    public AveragePartition(long start, long length) {
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
    
    public void event(long parameter) {
      total.add(parameter);
      count.increment();
    }
    
    public void aggregate(Average average) {
      average.total += total.sum();
      average.count += count.sum();
    }
  }
  
  static class Average {
    long total;
    long count;
  }
}

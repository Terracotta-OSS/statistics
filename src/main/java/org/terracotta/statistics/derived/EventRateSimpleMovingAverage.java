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

import static org.terracotta.statistics.Time.time;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.jsr166e.LongAdder;
import org.terracotta.statistics.observer.ChainedEventObserver;

/**
 *
 * @author cdennis
 */
public class EventRateSimpleMovingAverage implements ChainedEventObserver, ValueStatistic<Double> {

  private static final int PARTITION_COUNT = 10;

  private final Queue<CounterPartition> archive = new ConcurrentLinkedQueue<CounterPartition>();
  private final AtomicReference<CounterPartition> activePartition;
  
  private volatile long windowSize;
  private volatile long partitionSize;
  
  public EventRateSimpleMovingAverage(long time, TimeUnit unit) {
    this.windowSize  = unit.toNanos(time);
    this.partitionSize = windowSize / PARTITION_COUNT;
    this.activePartition = new AtomicReference<CounterPartition>(new CounterPartition(time(), partitionSize));
  }

  public void setWindow(long time, TimeUnit unit) {
    this.windowSize = unit.toNanos(time);
    this.partitionSize = windowSize / PARTITION_COUNT;
  }

  @Override
  public Double value() {
    return rateUsingSeconds();
  }
  
  public Double rateUsingSeconds() {
    final long endTime = time();
    final long startTime = endTime - windowSize;
    
    CounterPartition current = activePartition.get();
    long count;
    long actualStartTime = startTime;
    if (current.isBefore(startTime)) {
      count = 0;
    } else {
      count = current.sum();
      actualStartTime = Math.min(actualStartTime, current.start());
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
    
    if (count == 0L) {
      return 0.0;
    } else {
      return ((double) (TimeUnit.SECONDS.toNanos(1) * count)) / (endTime - actualStartTime);
    }
  }
  
  public Double rate(TimeUnit base) {
    return rateUsingSeconds() * ((double) base.toNanos(1) / TimeUnit.SECONDS.toNanos(1));
  }
  
  @Override
  public void event(long time, long ... parameters) {
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
    for (CounterPartition earliest = archive.peek(); earliest!=null && earliest.isBefore(startTime); earliest = archive.peek()) {
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

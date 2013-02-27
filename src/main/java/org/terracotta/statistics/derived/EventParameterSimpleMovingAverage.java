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
import org.terracotta.statistics.jsr166e.LongMaxUpdater;
import org.terracotta.statistics.observer.ChainedEventObserver;

/**
 *
 * @author cdennis
 */
public class EventParameterSimpleMovingAverage implements ChainedEventObserver {

  private static final int PARTITION_COUNT = 10;

  private final Queue<AveragePartition> archive = new ConcurrentLinkedQueue<AveragePartition>();
  private final AtomicReference<AveragePartition> activePartition;
  
  private volatile long windowSize;
  private volatile long partitionSize;
  
  public EventParameterSimpleMovingAverage(long time, TimeUnit unit) {
    this.windowSize  = unit.toNanos(time);
    this.partitionSize = windowSize / PARTITION_COUNT;
    this.activePartition = new AtomicReference<AveragePartition>(new AveragePartition(Long.MIN_VALUE, partitionSize));
  }

  public void setWindow(long time, TimeUnit unit) {
    this.windowSize = unit.toNanos(time);
    this.partitionSize = windowSize / PARTITION_COUNT;
  }

  public Double value() {
    return average();
  }

  public ValueStatistic<Double> averageStatistic() {
    return new ValueStatistic<Double>() {
      @Override
      public Double value() {
        return average();
      }
    };
  }
  
  public ValueStatistic<Long> minimumStatistic() {
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return minimum();
      }
    };
  }
  
  public ValueStatistic<Long> maximumStatistic() {
    return new ValueStatistic<Long>() {

      @Override
      public Long value() {
        return maximum();
      }
    };
  }
  
  public final double average() {
    long startTime = time() - windowSize;
    
    AveragePartition current = activePartition.get();
    if (current.isBefore(startTime)) {
      return Double.NaN;
    } else {
      Average average = new Average();
      current.aggregate(average);

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

      return ((double) average.total) / average.count;
    }
  }
  
  public final Long maximum() {
    long startTime = time() - windowSize;
    
    AveragePartition current = activePartition.get();
    if (current.isBefore(startTime)) {
      return null;
    } else {
      long maximum = current.maximum();
      for (Iterator<AveragePartition> it = archive.iterator(); it.hasNext(); ) {
        AveragePartition partition = it.next();
        if (partition == current) {
          break;
        } else if (partition.isBefore(startTime)) {
          it.remove();
        } else {
          maximum = Math.max(maximum, partition.maximum());
        }
      }

      return maximum;
    }
  }
  
  public final Long minimum() {
    long startTime = time() - windowSize;
    
    AveragePartition current = activePartition.get();
    if (current.isBefore(startTime)) {
      return null;
    } else {
      long minimum = current.minimum();
      for (Iterator<AveragePartition> it = archive.iterator(); it.hasNext(); ) {
        AveragePartition partition = it.next();
        if (partition == current) {
          break;
        } else if (partition.isBefore(startTime)) {
          it.remove();
        } else {
          minimum = Math.min(minimum, partition.minimum());
        }
      }

      return minimum;
    }
  }

  @Override
  public void event(long time, long ... parameters) {
    while (true) {
      AveragePartition partition = activePartition.get();
      if (partition.targetFor(time)) {
        partition.event(parameters[0]);
        return;
      } else {
        AveragePartition newPartition = new AveragePartition(time, partitionSize);
        if (activePartition.compareAndSet(partition, newPartition)) {
          archive(partition);
          newPartition.event(parameters[0]);
          return;
        }
      }
    }
  }

  private void archive(AveragePartition partition) {
    archive.add(partition);
    
    long startTime = partition.end() - windowSize;
    for (AveragePartition earliest = archive.peek(); earliest!=null && earliest.isBefore(startTime); earliest = archive.peek()) {
      if (archive.remove(earliest)) {
        break;
      }
    }
  }
  
  static class AveragePartition {

    private final LongAdder total = new LongAdder();
    private final LongAdder count = new LongAdder();
    private final LongMaxUpdater maximum = new LongMaxUpdater();
    private final LongMaxUpdater minimum = new LongMaxUpdater();
    
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
      maximum.update(parameter);
      minimum.update(-parameter);
    }
    
    public void aggregate(Average average) {
      average.total += total.sum();
      average.count += count.sum();
    }

    public long maximum() {
      return maximum.max();
    }
    
    public long minimum() {
      return -minimum.max();
    }
  }
  
  static class Average {
    long total;
    long count;
  }
}

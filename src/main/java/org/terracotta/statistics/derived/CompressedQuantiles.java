/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.statistics.derived;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Stream focused quantiles calculation.
 * 
 * Data Stream based quantiles algorithm from:
 * "Effective Computation of Biased Quantiles over Data Streams"
 * Graham Cormode, Flip Korn, S. Muthukrishnan, Divesh Srivastava
 */
public abstract class CompressedQuantiles {

  private final List<Sample> samples = new LinkedList<Sample>();
  
  private long totalValues = 0;
  
  public synchronized void insert(long[] values) {
    insert(values, 0, values.length);
  }
  
  public synchronized void insert(long[] values, int offset, int length) {
    Arrays.sort(values, offset, length);
    
    ListIterator<Sample> it = samples.listIterator();
    long rank = 0;
    for (int i = offset; i < offset + length; i++) {
      long value = values[i];
      
      while (it.hasNext()) {
        Sample next = it.next();
        if (value <= next.value) {
          it.previous();
          break;
        } else {
          rank += next.size;
        }
      }
      
      //insert here (either the end or between)
      Sample insert;
      if (it.hasNext() && it.hasPrevious()) {
        insert = new Sample(value, 1L, ((long) Math.floor(allowableError(rank, totalValues))) - 1L);
      } else {
        insert = new Sample(value, 1L, 0L);
      }
      it.add(insert);
      totalValues++;
      rank += insert.size;
    }

    compress();
  }

  public synchronized long query(double phi) {
    if (samples.size() < 2) {
      throw new IllegalStateException();
    }
    
    double targetRank = phi * totalValues;
    double threshold = targetRank + (allowableError(targetRank, totalValues) / 2.0);
    ListIterator<Sample> it = samples.listIterator();
    Sample previous = it.next();
    long rank = previous.size;
    while (it.hasNext()) {
      Sample current = it.next();
      if (rank + current.size + current.delta > threshold) {
        return previous.value;
      }
      previous = current;
      rank += previous.size;
    }
    return previous.value;
  }
  
  protected abstract double allowableError(double r, long n);

  private synchronized void compress() {
    if (samples.size() < 2) {
      return;
    }
    
    ListIterator<Sample> it = samples.listIterator(1);
    Sample current = it.next();
    long rank = current.size;
    while (it.hasNext()) {
      Sample next = it.next();
      double allowableError = allowableError(rank, totalValues);
      if (current.size + next.size + next.delta <= allowableError) {
        Sample merged = new Sample(next.value, current.size + next.size, next.delta);
        it.set(merged);
        if (it.previous() != merged) {
          throw new AssertionError();
        }
        if (it.previous() != current) {
          throw new AssertionError();
        }
        it.remove();
        current = it.next();
        rank += next.size;
      } else {
        current = next;
        rank += current.size;
      }
    }
  }
  
  @Override
  public String toString() {
    return "Total Values : " + totalValues + " Samples : " + samples.size() + " -- " + samples.toString();
  }
  
  static class Sample {
    
    long value;
    long delta;
    long size;

    private Sample(long value, long size, long delta) {
      this.value = value;
      this.size = size;
      this.delta = delta;
    }
    
    @Override
    public String toString() {
      return "sample:" + value + "(x" + size + ")(e=" + delta + ")";
    }
  }
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.archive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author cdennis
 */
public class StatisticArchive<T> implements SampleSink<Timestamped<T>> {
  
  private final CircularBuffer<Timestamped<T>> buffer;
  private final SampleSink<? super Timestamped<T>> overspill;
  
  public StatisticArchive(int size) {
    this(size, DevNull.DEV_NULL);
  }
  
  public StatisticArchive(int size, SampleSink<? super Timestamped<T>> overspill) {
    this.buffer = new CircularBuffer<Timestamped<T>>(size);
    this.overspill = overspill;
  }
  
  @Override
  public void accept(Timestamped<T> object) {
    overspill.accept(buffer.insert(object));
  }
  
  public List<Timestamped<T>> getArchive() {
    return Collections.unmodifiableList(Arrays.asList((Timestamped<T>[]) buffer.toArray(Timestamped[].class)));
  }
}

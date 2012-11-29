/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.statistics.archive;

import org.hamcrest.collection.IsEmptyCollection;
import static org.hamcrest.collection.IsIterableContainingInOrder.*;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 *
 * @author cdennis
 */
public class StatisticArchiveTest {
  
  @Test
  public void testEmptyArchive() {
    StatisticArchive<String> archive = new StatisticArchive<String>(2);
    assertThat(archive.getArchive(), IsEmptyCollection.<Timestamped<String>>empty());
  }
  
  @Test
  public void testOccupiedArchive() {
    StatisticArchive<String> archive = new StatisticArchive<String>(2);
    Timestamped<String> sample1 = new Sample("foo", 0);
    Timestamped<String> sample2 = new Sample("bar", 1);
    archive.accept(sample1);
    archive.accept(sample2);
    assertThat(archive.getArchive(), contains(sample1, sample2));
  }

  @Test
  public void testArchiveOverspill() {
    StatisticArchive<String> overspill = new StatisticArchive<String>(1);
    StatisticArchive<String> archive = new StatisticArchive<String>(1, overspill);
    Timestamped<String> sample1 = new Sample("foo", 0);
    Timestamped<String> sample2 = new Sample("bar", 1);
    archive.accept(sample1);
    archive.accept(sample2);
    assertThat(archive.getArchive(), contains(sample2));
    assertThat(overspill.getArchive(), contains(sample1));
  }
  
  static class Sample<T> implements Timestamped<T> {

    private final T sample;
    private final long timestamp;

    public Sample(T sample, long timestamp) {
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

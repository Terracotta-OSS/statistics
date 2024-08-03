/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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

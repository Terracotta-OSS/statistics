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
package org.terracotta.statistics.archive;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.terracotta.statistics.Sample;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author cdennis
 */
public class StatisticArchiveTest {

  @Test
  public void test_since() {
    StatisticArchive<String> archive = new StatisticArchive<>(2);
    Sample<String> sample1 = new Sample<>(1479819723336L, "foo");
    Sample<String> sample2 = new Sample<>(1479819721336L, "bar");
    archive.add(sample1);
    archive.add(sample2);
    assertThat(archive.getArchive(0).size(), equalTo(2));
  }

  @Test
  public void testEmptyArchive() {
    StatisticArchive<String> archive = new StatisticArchive<>(2);
    assertThat(archive.getArchive(), IsEmptyCollection.empty());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testOccupiedArchive() {
    StatisticArchive<String> archive = new StatisticArchive<>(2);
    Sample<String> sample1 = new Sample<>(0, "foo");
    Sample<String> sample2 = new Sample<>(1, "bar");
    archive.add(sample1);
    archive.add(sample2);
    assertThat(archive.getArchive(), contains(sample1, sample2));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testArchiveOverspill() {
    StatisticArchive<String> overspill = new StatisticArchive<>(1);
    StatisticArchive<String> archive = new StatisticArchive<>(1, overspill::add);
    Sample<String> sample1 = new Sample<>(0, "foo");
    Sample<String> sample2 = new Sample<>(1, "bar");
    archive.add(sample1);
    archive.add(sample2);
    assertThat(archive.getArchive(), contains(sample2));
    assertThat(overspill.getArchive(), contains(sample1));
  }

}

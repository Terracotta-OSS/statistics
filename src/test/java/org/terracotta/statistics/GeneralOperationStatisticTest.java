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

package org.terracotta.statistics;

import org.junit.Test;
import org.terracotta.util.Outcome;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GeneralOperationStatisticTest {

  private final GeneralOperationStatistic<Outcome> statistic = new GeneralOperationStatistic<>("outcome", Collections.emptySet(), Collections.emptyMap(), Outcome.class);

  private final Outcome[] outcomes = new Outcome[] { Outcome.GOOD, Outcome.BAD, Outcome.BAD };

  private void addStats() {
    Arrays.stream(outcomes)
        .forEach(outcome -> {
          statistic.begin();
          statistic.end(outcome);
        });
  }

  @Test
  public void count_empty() {
    assertThat(statistic.count(Outcome.GOOD), is(0L));
    assertThat(statistic.count(Outcome.BAD), is(0L));
    assertThat(statistic.count(Outcome.UGLY), is(0L));
  }

  @Test
  public void count() {
    addStats();

    assertThat(statistic.count(Outcome.GOOD), is(1L));
    assertThat(statistic.count(Outcome.BAD), is(2L));
    assertThat(statistic.count(Outcome.UGLY), is(0L));
  }


  @Test
  public void sum_empty() {
    assertThat(statistic.sum(EnumSet.allOf(Outcome.class)), is(0L));
  }

  @Test
  public void sum() {
    addStats();

    assertThat(statistic.sum(Collections.singleton(Outcome.GOOD)), is(1L));
    assertThat(statistic.sum(Collections.singleton(Outcome.BAD)), is(2L));
    assertThat(statistic.sum(Collections.singleton(Outcome.UGLY)), is(0L));
    assertThat(statistic.sum(EnumSet.allOf(Outcome.class)), is(3L));
  }

  @Test
  public void testToString() {
    addStats();

    assertThat(statistic.toString(), is("[GOOD=1, BAD=2, UGLY=0]"));
  }
}
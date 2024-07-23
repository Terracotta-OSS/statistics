/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.terracotta.statistics.ValueStatistics.gauge;
import static org.terracotta.statistics.ValueStatistics.memoize;

/**
 * @author Mathieu Carbou
 */
public class SuppliedValueStatisticTest {

  @Test
  public void test_memoize() throws Exception {
    ValueStatistic<Long> s = memoize(500, TimeUnit.MILLISECONDS, gauge(Time::absoluteTime));
    long now = s.value();
    sleep(100);
    long nowAgain = s.value();
    assertThat(now, equalTo(nowAgain));
    sleep(401);
    nowAgain = s.value();
    assertThat(now, not(equalTo(nowAgain)));
  }

}
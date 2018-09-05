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

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.EnumSet.of;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author cdennis
 */
public class OperationResultSamplerTest {

  @Test
  public void testRateOfZeroNeverSamples() {
    OperationResultSampler<FooBar> sampler = new OperationResultSampler<>(of(FooBar.FOO), 0.0);
    sampler.addDerivedStatistic((time, slatency) -> fail());

    for (int i = 0; i < 100; i++) {
      sampler.begin(0);
      sampler.end(1, 0, FooBar.FOO);
    }
  }

  @Test
  public void testRateOfOneAlwaysSamples() {
    OperationResultSampler<FooBar> sampler = new OperationResultSampler<>(of(FooBar.FOO), 1.0);
    final AtomicInteger eventCount = new AtomicInteger();
    sampler.addDerivedStatistic((time, latency) -> eventCount.incrementAndGet());

    for (int i = 0; i < 100; i++) {
      sampler.begin(0);
      sampler.end(1, 0, FooBar.FOO);
    }

    assertThat(eventCount.get(), is(100));
  }

  @Test
  public void testMismatchedResultNeverSamples() {
    OperationResultSampler<FooBar> sampler = new OperationResultSampler<>(of(FooBar.FOO), 1.0);
    sampler.addDerivedStatistic((time, latency) -> fail());

    for (int i = 0; i < 100; i++) {
      sampler.begin(0);
      sampler.end(1, 0, FooBar.BAR);
    }
  }

  @Test
  public void testLatencyMeasuredAccurately() {
    Random random = new Random();
    OperationResultSampler<FooBar> sampler = new OperationResultSampler<>(of(FooBar.FOO), 1.0);
    final AtomicLong expected = new AtomicLong();
    sampler.addDerivedStatistic((time, latency) -> assertThat(latency, greaterThanOrEqualTo(expected.get())));

    for (int i = 0; i < 10; i++) {
      long start = random.nextLong();
      int sleep = random.nextInt(100);
      sampler.begin(start);
      sampler.end(start + sleep, 0, FooBar.FOO);
    }
  }

  enum FooBar {
    FOO, BAR
  }
}

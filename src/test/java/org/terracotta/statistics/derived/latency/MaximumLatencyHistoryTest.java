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
package org.terracotta.statistics.derived.latency;

import org.junit.Test;
import org.terracotta.statistics.Sample;
import org.terracotta.statistics.StatisticType;
import org.terracotta.statistics.Time;

import java.util.List;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Mathieu Carbou
 */
public class MaximumLatencyHistoryTest {

  @Test
  public void type() {
    MaximumLatencyHistory latencyHistory = new MaximumLatencyHistory(2, 400, MILLISECONDS, Time::absoluteTime);
    assertThat(latencyHistory.type(), equalTo(StatisticType.GAUGE));
  }

  @Test
  public void value() throws InterruptedException {
    MaximumLatencyHistory latencyHistory = new MaximumLatencyHistory(2, 400, MILLISECONDS, Time::absoluteTime);

    assertThat(latencyHistory.value(), equalTo(null));

    latencyHistory.event(Time.time(), 1);
    latencyHistory.event(Time.time(), 3);

    assertThat(latencyHistory.value(), equalTo(3L));

    sleep(500);

    assertThat(latencyHistory.value(), equalTo(null));

    latencyHistory.event(Time.time(), 2);

    assertThat(latencyHistory.value(), equalTo(2L));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void sink() throws InterruptedException {
    Consumer<LatencyPeriodAccumulator> sink = mock(Consumer.class);
    MaximumLatencyHistory latencyHistory = new MaximumLatencyHistory(2, 100, MILLISECONDS, Time::absoluteTime, sink);

    latencyHistory.event(Time.time(), 1);
    latencyHistory.event(Time.time(), 3);
    latencyHistory.event(Time.time(), 4);
    assertThat(latencyHistory.value(), equalTo(4L));

    sleep(200);

    latencyHistory.event(Time.time(), 1);
    latencyHistory.event(Time.time(), 3);
    assertThat(latencyHistory.value(), equalTo(3L));

    sleep(200);

    latencyHistory.event(Time.time(), 1);
    assertThat(latencyHistory.value(), equalTo(1L));

    verify(sink, times(1)).accept(any(LatencyPeriodAccumulator.class));
  }

  @Test
  public void history() throws InterruptedException {
    MaximumLatencyHistory latencyHistory = new MaximumLatencyHistory(2, 100, MILLISECONDS, Time::absoluteTime);

    latencyHistory.event(Time.time(), 1);
    latencyHistory.event(Time.time(), 3);
    latencyHistory.event(Time.time(), 4);

    sleep(200);

    long t1 = Time.absoluteTime();
    latencyHistory.event(Time.time(), 1);
    latencyHistory.event(Time.time(), 3);

    sleep(200);

    long t2 = Time.absoluteTime();
    latencyHistory.event(Time.time(), 1);

    List<Sample<Long>> history = latencyHistory.history();

    assertThat(history.size(), equalTo(2));
    assertThat(history.get(0).getSample(), equalTo(3L));
    assertThat(history.get(0).getTimestamp() - t1, lessThan(10L));
    assertThat(history.get(1).getSample(), equalTo(1L));
    assertThat(history.get(1).getTimestamp() - t2, lessThan(10L));
  }

  @Test
  public void historySince() throws InterruptedException {
    MaximumLatencyHistory latencyHistory = new MaximumLatencyHistory(2, 100, MILLISECONDS, Time::absoluteTime);

    long t1 = Time.absoluteTime();
    latencyHistory.event(Time.time(), 3);
    sleep(200);

    long t2 = Time.absoluteTime();
    latencyHistory.event(Time.time(), 1);

    List<Sample<Long>> history = latencyHistory.history(t1 - 10);

    assertThat(history.size(), equalTo(2));
    assertThat(history.get(0).getSample(), equalTo(3L));
    assertThat(history.get(0).getTimestamp() - t1, lessThan(10L));
    assertThat(history.get(1).getSample(), equalTo(1L));
    assertThat(history.get(1).getTimestamp() - t2, lessThan(10L));

    history = latencyHistory.history(t2 - 10);
    assertThat(history.size(), equalTo(1));
    assertThat(history.get(0).getSample(), equalTo(1L));
    assertThat(history.get(0).getTimestamp() - t2, lessThan(10L));
  }

}

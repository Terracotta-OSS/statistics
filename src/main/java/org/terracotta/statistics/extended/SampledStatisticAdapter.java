/*
 * Copyright Terracotta, Inc.
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

package org.terracotta.statistics.extended;

import org.terracotta.statistics.Time;
import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.archive.Sample;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Mathieu Carbou
 */
public class SampledStatisticAdapter<T extends Serializable> implements SampledStatistic<T> {

  private final Supplier<Long> timeSource;
  private final ValueStatistic<T> statistic;

  private SampledStatisticAdapter(ValueStatistic<T> statistic, Supplier<Long> timeSource) {
    this.statistic = Objects.requireNonNull(statistic);
    this.timeSource = Objects.requireNonNull(timeSource);
  }

  @Override
  public T value() {
    return statistic.value();
  }

  @Override
  public List<Sample<T>> history() {
    return Collections.singletonList(new Sample<>(timeSource.get(), statistic.value()));
  }

  @Override
  public List<Sample<T>> history(long since) {
    long now = timeSource.get();
    return since <= now ? Collections.singletonList(new Sample<>(now, statistic.value())) : Collections.emptyList();
  }

  @Override
  public StatisticType type() {
    return statistic.type();
  }

  public static <T extends Serializable> SampledStatistic<T> sample(ValueStatistic<T> accessor, Supplier<Long> timeSource) {
    return new SampledStatisticAdapter<>(accessor, timeSource);
  }

  public static <T extends Serializable> SampledStatistic<T> sample(ValueStatistic<T> accessor) {
    return new SampledStatisticAdapter<>(accessor, Time::absoluteTime);
  }
}

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
package org.terracotta.statistics.registry;

import org.terracotta.statistics.Sample;
import org.terracotta.statistics.SampledStatistic;
import org.terracotta.statistics.StatisticType;
import org.terracotta.statistics.Table;
import org.terracotta.statistics.ValueStatistic;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @author Mathieu Carbou
 */
public class Statistic<T extends Serializable> implements Serializable {

  private static final long serialVersionUID = 1L;

  private final StatisticType type;
  private final List<Sample<T>> samples;

  public Statistic(StatisticType type) {
    this(type, Collections.emptyList());
  }

  public Statistic(StatisticType type, Sample<T> sample) {
    this(type, Collections.singletonList(sample));
  }

  public Statistic(StatisticType type, List<Sample<T>> samples) {
    this.type = type;
    this.samples = samples;
  }

  public StatisticType getType() {
    return type;
  }

  public boolean isEmpty() {
    return samples.isEmpty();
  }

  public List<Sample<T>> getSamples() {
    return samples;
  }

  public Optional<T> getLatestSampleValue() {
    return getLatestSample().map(Sample::getSample);
  }

  public Optional<Sample<T>> getLatestSample() {
    return samples.isEmpty() ? Optional.empty() : Optional.of(samples.get(samples.size() - 1));
  }

  @Override
  public String toString() {
    return "Statistic{" +
        "" + "type=" + type +
        ", samples=" + samples +
        '}';
  }

  static <T extends Serializable> Statistic<T> extract(ValueStatistic<T> valueStatistic, long sinceMillis, long now) {
    // sample stats
    if (valueStatistic instanceof SampledStatistic) {
      return new Statistic<>(
          valueStatistic.type(),
          ((SampledStatistic<T>) valueStatistic).history(sinceMillis)
              .stream()
              .filter(s -> accepted(s.getSample()))
              .collect(toList()));
    }
    // single-value history ?
    if (sinceMillis <= now) {
      T value = valueStatistic.value();
      if (accepted(value)) {
        return new Statistic<>(valueStatistic.type(), new Sample<>(now, value));
      }
    }
    // empty history
    return new Statistic<>(valueStatistic.type());
  }

  private static <T extends Serializable> boolean accepted(T sample) {
    // we do not accept null values for statistics - it means that it is not available right now
    // we do not accept empty tables
    return sample != null && !(sample instanceof Table && ((Table) sample).isEmpty());
  }

}

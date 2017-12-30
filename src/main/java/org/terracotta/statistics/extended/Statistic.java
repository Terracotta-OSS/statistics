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

import org.terracotta.statistics.archive.Sample;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author Mathieu Carbou
 */
public class Statistic<T extends Serializable> implements Serializable {

  private static final long serialVersionUID = 1L;

  private final StatisticType type;
  private final List<Sample<T>> samples;

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

  public Optional<T> getLatestSample() {
    return samples.isEmpty() ? Optional.empty() : Optional.of(samples.get(samples.size() - 1).getSample());
  }

  @Override
  public String toString() {
    return "Statistic{" +
        "" + "type=" + type +
        ", samples=" + samples +
        '}';
  }
}

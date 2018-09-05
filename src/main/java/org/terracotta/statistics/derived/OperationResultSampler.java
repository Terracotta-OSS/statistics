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

import org.terracotta.statistics.observer.ChainedEventObserver;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author cdennis
 */
public class OperationResultSampler<T extends Enum<T>> extends OperationResultFilter<T> {

  private final int ceiling;

  public OperationResultSampler(Set<T> targets, double sampling, ChainedEventObserver... observers) {
    super(targets, observers);
    if (sampling > 1.0 || sampling < 0.0) {
      throw new IllegalArgumentException("Sampling must be between 0.0 and 1.0");
    }
    this.ceiling = (int) (Integer.MAX_VALUE * sampling);
  }

  @Override
  public void end(long time, long latency, T result) {
    if (!derivedStatistics.isEmpty() && targets.contains(result) && sample()) {
      for (ChainedEventObserver derived : derivedStatistics) {
        derived.event(time, latency);
      }
    }
  }

  private boolean sample() {
    return ceiling == Integer.MAX_VALUE || ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) < ceiling;
  }

  public int getCeiling() {
    return ceiling;
  }
}

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
package org.terracotta.statistics.derived;

import org.terracotta.statistics.AbstractSourceStatistic;
import org.terracotta.statistics.observer.ChainedEventObserver;
import org.terracotta.statistics.observer.ChainedOperationObserver;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author cdennis
 */
public class OperationResultFilter<T extends Enum<T>> extends AbstractSourceStatistic<ChainedEventObserver> implements ChainedOperationObserver<T> {

  protected final Set<T> targets;

  public OperationResultFilter(Set<T> targets, ChainedEventObserver... observers) {
    this.targets = EnumSet.copyOf(targets);
    for (ChainedEventObserver observer : observers) {
      addDerivedStatistic(observer);
    }
  }

  @Override
  public void begin(long time) {
    //no-op
  }

  @Override
  public void end(long time, long latency, T result) {
    if (!derivedStatistics.isEmpty() && targets.contains(result)) {
      for (ChainedEventObserver derived : derivedStatistics) {
        derived.event(time, latency);
      }
    }
  }

  public Set<T> getTargets() {
    return Collections.unmodifiableSet(targets);
  }
}

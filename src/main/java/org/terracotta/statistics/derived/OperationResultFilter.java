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

import java.util.EnumSet;
import java.util.Set;

import org.terracotta.statistics.AbstractSourceStatistic;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.observer.OperationObserver;

/**
 *
 * @author cdennis
 */
public class OperationResultFilter<T extends Enum<T>> extends AbstractSourceStatistic<EventObserver> implements OperationObserver<T> {

  private final Set<T> targets;

  public OperationResultFilter(Set<T> targets, EventObserver ... observers) {
    this.targets = EnumSet.copyOf(targets);
    for (EventObserver observer : observers) {
      addDerivedStatistic(observer);
    }
  }
  
  @Override
  public void begin() {
    //no-op
  }

  @Override
  public void end(T result) {
    if (targets.contains(result)) {
      for (EventObserver derived : derived()) {
        derived.event();
      }
    }
  }

  @Override
  public void end(T result, long ... parameters) {
    if (targets.contains(result)) {
      for (EventObserver derived : derived()) {
        derived.event(parameters);
      }
    }
  }
  
}

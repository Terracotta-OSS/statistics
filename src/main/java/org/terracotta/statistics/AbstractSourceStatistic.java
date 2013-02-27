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

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.terracotta.statistics.observer.ChainedObserver;

/**
 * An abstract {@code SourceStatistic} that handles derived statistic
 * (de)registration.
 * <p>
 * This implementation exposes the currently registered statistics via the
 * {@link #derived()} method.  Concrete implementations of this class should
 * fire on the contents of this {@code Iterable} to update the derived statistics.
 */
public class AbstractSourceStatistic<T extends ChainedObserver> implements SourceStatistic<T> {

  protected final Collection<T> derivedStatistics = new CopyOnWriteArrayList<T>();

  @Override
  public void addDerivedStatistic(T derived) {
    derivedStatistics.add(derived);
  }

  @Override
  public void removeDerivedStatistic(T derived) {
    derivedStatistics.remove(derived);
  }
}

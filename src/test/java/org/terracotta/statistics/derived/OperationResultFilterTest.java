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

import org.junit.Test;
import org.terracotta.statistics.observer.ChainedEventObserver;
import org.terracotta.util.Outcome;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class OperationResultFilterTest {

  private OperationResultFilter<Outcome> filter;

  @Test
  public void end() {
  }

  @Test
  public void constructorTest() {
    Set<Outcome> outcomes = new HashSet<>();
    outcomes.add(Outcome.GOOD);
    outcomes.add(Outcome.BAD);

    ChainedEventObserver observer1 = mock(ChainedEventObserver.class);
    ChainedEventObserver observer2 = mock(ChainedEventObserver.class);

    filter = new OperationResultFilter<>(outcomes, observer1, observer2);
    assertThat(filter.getTargets(), contains(Outcome.GOOD, Outcome.BAD));
    assertThat(filter.getDerivedStatistics(), contains(observer1, observer2));
  }

  @Test
  public void eventCalled_noObservers() {
    Set<Outcome> outcomes = Collections.singleton(Outcome.GOOD);

    filter = new OperationResultFilter<>(outcomes);
    filter.end(10, 20, Outcome.GOOD);
  }

  @Test
  public void eventCalled_trackedTarget() {
    Set<Outcome> outcomes = Collections.singleton(Outcome.GOOD);

    ChainedEventObserver observer1 = mock(ChainedEventObserver.class);
    ChainedEventObserver observer2 = mock(ChainedEventObserver.class);

    filter = new OperationResultFilter<>(outcomes, observer1, observer2);
    filter.end(10, 20, Outcome.GOOD);

    verify(observer1).event(10 ,20);
    verify(observer2).event(10 ,20);
  }

  @Test
  public void eventCalled_untrackedTarget() {
    Set<Outcome> outcomes = Collections.singleton(Outcome.GOOD);

    ChainedEventObserver observer1 = mock(ChainedEventObserver.class);
    ChainedEventObserver observer2 = mock(ChainedEventObserver.class);

    filter = new OperationResultFilter<>(outcomes, observer1, observer2);
    filter.end(10, 20, Outcome.BAD);

    verifyNoMoreInteractions(observer1, observer2);
  }
}
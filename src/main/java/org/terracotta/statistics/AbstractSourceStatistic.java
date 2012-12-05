/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.terracotta.statistics.observer.Observer;

/**
 * An abstract {@code SourceStatistic} that handles derived statistic
 * (de)registration.
 * <p>
 * This implementation exposes the currently registered statistics via the
 * {@link #derived()} method.  Concrete implementations of this class should
 * fire on the contents of this {@code Iterable} to update the derived statistics.
 */
public class AbstractSourceStatistic<T extends Observer> implements SourceStatistic<T> {

  private final Collection<T> derivedStatistics = new CopyOnWriteArrayList<T>();

  @Override
  public void addDerivedStatistic(T derived) {
    derivedStatistics.add(derived);
  }

  @Override
  public void removeDerivedStatistic(T derived) {
    derivedStatistics.remove(derived);
  }

  /**
   * Returns the registered derived statistics.
   * 
   * @return derived statistics
   */
  protected Iterable<T> derived() {
    return derivedStatistics;
  }
}

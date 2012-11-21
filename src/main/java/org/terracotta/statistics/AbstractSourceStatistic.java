/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.terracotta.statistics.SourceStatistic;
import org.terracotta.statistics.observer.Observer;

public class AbstractSourceStatistic<T extends Observer> implements SourceStatistic<T> {

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

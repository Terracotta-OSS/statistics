/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.derived;

import org.terracotta.statistics.AbstractSourceStatistic;
import org.terracotta.statistics.observer.EventObserver;
import org.terracotta.statistics.observer.OperationObserver;

/**
 *
 * @author cdennis
 */
public class OperationResultFilter<T extends Enum<T>> extends AbstractSourceStatistic<EventObserver> implements OperationObserver<T> {

  private final T target;

  public OperationResultFilter(T target, EventObserver ... observers) {
    this.target = target;
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
    if (target.equals(result)) {
      for (EventObserver derived : derived()) {
        derived.event();
      }
    }
  }

  @Override
  public void end(T result, long ... parameters) {
    if (target.equals(result)) {
      for (EventObserver derived : derived()) {
        derived.event(parameters);
      }
    }
  }
  
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import org.terracotta.statistics.observer.Observer;

/**
 * Source statistic implementations support derived statistics.
 * <p>
 * Derived statistics can be registered and will then receive the relevant
 * observer calls to update their status.
 * 
 * @param <T> Supported derived observer type
 */
public interface SourceStatistic<T extends Observer> {
  
  /**
   * Register the given {@code Observer} to be called by this {@code SourceStatistic}
   * 
   * @param derived statistic to be registered
   */
  void addDerivedStatistic(T derived);
  
  /**
   * Remove the given registered {@Observer} from this {@code SourceStatistic}.
   * 
   * @param derived statistic to be removed
   */
  void removeDerivedStatistic(T derived);
}

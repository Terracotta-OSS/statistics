/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import org.terracotta.statistics.observer.Observer;

public interface SourceStatistic<T extends Observer> {
  
  void addDerivedStatistic(T derived);
  
  void removeDerivedStatistic(T derived);
}

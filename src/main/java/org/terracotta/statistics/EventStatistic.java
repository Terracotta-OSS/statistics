/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import org.terracotta.statistics.observer.EventObserver;

public interface EventStatistic extends SourceStatistic<EventObserver> {
  
  /*
   * very cheap to maintain - increment done in user thread - summation across stripes done on reading
   */
  long count();
}

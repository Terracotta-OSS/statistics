/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import org.terracotta.statistics.observer.OperationObserver;

public interface OperationStatistic<T extends Enum<T>> extends SourceStatistic<OperationObserver<? super T>> {

  Class<T> type();
  
  String name();

  /*
   * very cheap to maintain - increment done in user thread - summation across stripes done on reading
   */
  long count(T type);
}

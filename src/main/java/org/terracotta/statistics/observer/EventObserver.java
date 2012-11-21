/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.observer;

public interface EventObserver extends Observer {
  
  /*
   * The parameter argument is really a constrained and collapsed to primitive
   * generic type with no bounds.  So the "real" class declaration should be:
   * @{code interface EventObserver<T>}
   */
  
  void event(long parameter);
}

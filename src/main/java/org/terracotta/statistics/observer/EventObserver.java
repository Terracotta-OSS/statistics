/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.observer;

/**
 * Event observers track the occurrence of singular events.
 * <p>
 * Events can have an associated parameter the use of which is left up to
 * the implementors of both the producer and consumer of events.
 */
public interface EventObserver extends Observer {
  
  /*
   * The parameter argument is really a constrained and collapsed to primitive
   * generic type with no bounds.  So the "real" class declaration should be:
   * @{code interface EventObserver<T>}
   */
  
  /**
   * Called to indicate an event happened.
   * 
   * @param parameter the event parameter
   */
  void event(long parameter);
}

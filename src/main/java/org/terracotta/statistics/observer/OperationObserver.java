/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.observer;

/**
 * Operation observers track the occurrence of processes which take a finite time
 * and can potential terminate in different ways.
 * <p>
 * Operations must have an associated enum type that represents their possible
 * outcomes.  An example of such an enum type would be:
 * <pre>
 * enum PlaneFlight {
 *   LAND, CRASH;
 * }
 * </pre>
 * Operations also have an associated parameter the use of which is left up to
 * the implementors of both the producer and consumer of events.
 * 
 * @param <T> Enum type representing the possible operations 'results'
 */
public interface OperationObserver<T extends Enum<T>> extends Observer {
  
  /**
   * Called immediately prior to the operation beginning.
   */
  void begin();
  
  /**
   * Called immediately after the operation completes with no interesting parameters.
   * 
   * @param result the operation result
   */
  void end(T result);
  
  /**
   * Called immediately after the operation completes.
   * 
   * @param result the operation result
   * @param parameters the operation parameters
   */
  void end(T result, long ... parameters);
}

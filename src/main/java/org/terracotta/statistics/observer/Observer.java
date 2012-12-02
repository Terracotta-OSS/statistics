/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.observer;

/**
 * The marker interface implemented by all statistic observer classes.
 * <p>
 * A statistic observer presents methods used to update a statistic.  There are
 * two general classes of observer implementations:
 * <ol>
 *   <li>Initial observers are called by product code in order to record the
 * occurrence of a product related 'event'</li>
 *   <li>Derived observers are called by other observers with then intention of
 * tracking higher order statistics.</li>
 * </ol>
 */
public interface Observer {
  
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.archive;

/**
 *
 * @author cdennis
 */
public interface Timestamped<T> {
  
  T getSample();
  
  long getTimestamp();
}

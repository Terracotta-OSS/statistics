/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.observer;

public interface EventObserver extends Observer {
  
  void event(long parameter);
}

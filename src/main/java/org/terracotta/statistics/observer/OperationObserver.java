/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.observer;

public interface OperationObserver<T extends Enum<T>> extends Observer {
  
  void begin();
  
  void end(T result);
  
  void end(T result, long parameter);
}

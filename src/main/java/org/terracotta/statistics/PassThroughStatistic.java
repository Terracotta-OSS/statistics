/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class PassThroughStatistic<T extends Number> {
  
  private final Callable<T> source;
  
  public PassThroughStatistic(Callable<T> source) {
    this.source = source;
  }

  public T value() throws ExecutionException {
    try {
      return source.call();
    } catch (Exception ex) {
      throw new ExecutionException(ex);
    }
  }
}

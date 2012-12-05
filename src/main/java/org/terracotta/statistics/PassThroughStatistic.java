/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

import java.util.concurrent.Callable;

class PassThroughStatistic<T extends Number> implements ValueStatistic<T> {
  
  private final Callable<T> source;
  
  public PassThroughStatistic(Callable<T> source) {
    this.source = source;
  }

  @Override
  public T value() {
    try {
      return source.call();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}

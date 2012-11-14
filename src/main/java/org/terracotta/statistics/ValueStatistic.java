/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

public interface ValueStatistic<T extends Number> {
  
  T value();
}

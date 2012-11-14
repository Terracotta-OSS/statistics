/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

public interface Distribution<T extends Number> {
  
  T minimum();
  
  Number mean();
  
  T maximum();
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

/**
 *
 * @author cdennis
 */
public final class Time {
  
  private Time() {
    //static
  }

  public static long time() {
    return System.nanoTime();
  }
  
  public static long absoluteTime() {
    return System.currentTimeMillis();
  }
}

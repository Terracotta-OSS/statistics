/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics;

/**
 * This class contains the static time-sources used within the framework.
 */
public final class Time {
  
  private Time() {
    //static
  }

  /**
   * Returns a timestamp in nanoseconds with an arbitrary origin suitable for
   * timing purposes.
   * <p>
   * This contract is non-coincidentally reminiscent of 
   * {@link System#nanoTime()}.
   * 
   * @return a time in nanoseconds
   */
  public static long time() {
    return System.nanoTime();
  }
  
  /**
   * Returns a timestamp in milliseconds whose origin is at the Unix Epoch.
   * <p>
   * This contract is non-coincidentally reminiscent of 
   * {@link System#currentTimeMillis()}.
   * 
   * @return a Unix timestamp
   */
  public static long absoluteTime() {
    return System.currentTimeMillis();
  }
}

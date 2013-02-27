/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics;

/**
 * This class contains the static time-sources used within the framework.
 */
public final class Time {
  
  private static volatile TimeSource TIME_SOURCE = new TimeSource() {

    @Override
    public long time() {
      return System.nanoTime();
    }

    @Override
    public long absoluteTime() {
      return System.currentTimeMillis();
    }
  };

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
    return TIME_SOURCE.time();
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
    return TIME_SOURCE.absoluteTime();
  }
  
  public interface TimeSource {
    long time();

    long absoluteTime();
  }
}

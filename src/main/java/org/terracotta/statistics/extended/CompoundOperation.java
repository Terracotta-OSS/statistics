/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics.extended;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The Interface Operation.
 *
 * @param <T> the generic type
 */
public interface CompoundOperation<T extends Enum<T>> {

  /**
   * Type.
   *
   * @return the class
   */
  Class<T> type();

  /**
   * Component.
   *
   * @param result the result
   * @return the result
   */
  Result component(T result);

  /**
   * Compound.
   *
   * @param results the results
   * @return the result
   */
  Result compound(Set<T> results);

  /**
   * Count operation.
   *
   * @return the count operation
   */
  CountOperation<T> asCountOperation();

  /**
   * Ratio of.
   *
   * @param numerator  the numerator
   * @param denomiator the denomiator
   * @return the statistic
   */
  SampledStatistic<Double> ratioOf(Set<T> numerator, Set<T> denomiator);

  /**
   * Sets the always on.
   *
   * @param enable the new always on
   */
  void setAlwaysOn(boolean enable);

  /**
   * Checks if is always on.
   *
   * @return true, if is always on
   */
  boolean isAlwaysOn();

  /**
   * Sets the window.
   *
   * @param time the time
   * @param unit the unit
   */
  void setWindow(long time, TimeUnit unit);

  /**
   * Sets the history.
   *
   * @param samples the samples
   * @param time    the time
   * @param unit    the unit
   */
  void setHistory(int samples, long time, TimeUnit unit);

  /**
   * Gets the window size.
   *
   * @param unit the unit
   * @return the window size
   */
  long getWindowSize(TimeUnit unit);

  /**
   * Gets the history sample size.
   *
   * @return the history sample size
   */
  int getHistorySampleSize();

  /**
   * Gets the history sample time.
   *
   * @param unit the unit
   * @return the history sample time
   */
  long getHistorySampleTime(TimeUnit unit);

}

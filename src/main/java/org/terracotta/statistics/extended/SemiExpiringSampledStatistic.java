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

import org.terracotta.statistics.Time;
import org.terracotta.statistics.ValueStatistic;
import org.terracotta.statistics.archive.Timestamped;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Statistic implementation that stops sampling history if the last history access is
 * before a user supplied timestamp.
 *
 * @param <T> statistic type
 * @author Chris Dennis
 */
public class SemiExpiringSampledStatistic<T extends Number> extends AbstractSampledStatistic<T> implements SamplingSupport {

  /**
   * The active.
   */
  private boolean active = false;

  /**
   * The touch timestamp.
   */
  private long touchTimestamp = -1;

  private volatile boolean alwaysOn = false;

  /**
   * Creates a new semi-expiring statistic.
   *
   * @param source          statistic source
   * @param executor        executor to use for sampling
   * @param historySize     size of sample history
   * @param historyTime     period between samples
   * @param historyTimeUnit unit of period between samples
   * @param type            sampling type
   */
  public SemiExpiringSampledStatistic(ValueStatistic<T> source, ScheduledExecutorService executor, int historySize, long historyTime, TimeUnit historyTimeUnit, StatisticType type) {
    super(source, executor, historySize, historyTime, historyTimeUnit, type);
  }

  @Override
  public List<Timestamped<T>> history() {
    touch();
    return super.history();
  }

  @Override
  public List<Timestamped<T>> history(long since) {
    touch();
    return super.history(since);
  }

  @Override
  public final synchronized boolean active() {
    return active;
  }

  /**
   * Touch.
   */
  protected final synchronized void touch() {
    touchTimestamp = Time.absoluteTime();
    start();
  }

  /**
   * Start.
   */
  protected final synchronized void start() {
    if (!active) {
      startStatistic();
      startSampling();
      active = true;
    }
  }

  /**
   * Expire.
   *
   * @param expiry the expiry
   * @return true, if successful
   */
  public final synchronized boolean expire(long expiry) {
    if (alwaysOn) {
      return false;
    }
    if (touchTimestamp < expiry) {
      if (active) {
        stopSampling();
        stopStatistic();
        active = false;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void setAlwaysOn(boolean enable) {
    this.alwaysOn = enable;
    if (enable) {
      start();
    }
  }

  /**
   * Stop statistic.
   */
  protected void stopStatistic() {
    //no-op
  }

  /**
   * Start statistic.
   */
  protected void startStatistic() {
    //no-op
  }
}

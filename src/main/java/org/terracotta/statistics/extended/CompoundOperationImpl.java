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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.terracotta.statistics.OperationStatistic;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Class CompoundOperationImpl.
 *
 * @param <T> the generic type
 * @author cdennis
 */
public class CompoundOperationImpl<T extends Enum<T>> implements CompoundOperation<T> {

  private final OperationStatistic<T> source;

  private final Class<T> type;
  private final Map<T, ResultImpl<T>> operations;
  private final ConcurrentMap<Set<T>, ResultImpl<T>> compounds = new ConcurrentHashMap<>();
  private final ConcurrentMap<List<Set<T>>, ExpiringSampledStatistic<Double>> ratios = new ConcurrentHashMap<>();

  private final ScheduledExecutorService executor;

  private volatile long averagePeriod;
  private volatile TimeUnit averageTimeUnit;
  private volatile int historySize;
  private volatile long historyPeriod;
  private volatile TimeUnit historyTimeUnit;

  private volatile boolean alwaysOn = false;

  /**
   * Instantiates a new compound operation impl.
   *
   * @param source        the source
   * @param type          the type
   * @param averagePeriod the average period
   * @param averageTimeUnit   the average unit
   * @param executor      the executor
   * @param historySize   the history size
   * @param historyPeriod the history period
   * @param historyTimeUnit   the history unit
   */
  public CompoundOperationImpl(OperationStatistic<T> source, Class<T> type, long averagePeriod, TimeUnit averageTimeUnit,
                               ScheduledExecutorService executor, int historySize, long historyPeriod, TimeUnit historyTimeUnit) {
    this.type = type;
    this.source = source;

    this.averagePeriod = averagePeriod;
    this.averageTimeUnit = averageTimeUnit;
    this.executor = executor;
    this.historySize = historySize;
    this.historyPeriod = historyPeriod;
    this.historyTimeUnit = historyTimeUnit;

    this.operations = new EnumMap<>(type);
    for (T result : type.getEnumConstants()) {
      operations.put(result, new ResultImpl<>(source, EnumSet.of(result), averagePeriod, averageTimeUnit, executor, historySize, historyPeriod, historyTimeUnit));
    }
  }

  @Override
  public Class<T> type() {
    return type;
  }

  @Override
  public Result component(T result) {
    return operations.get(result);
  }

  @Override
  public Result compound(EnumSet<T> results) {
    if (results.size() == 1) {
      return component(results.iterator().next());
    } else {
      Set<T> key = EnumSet.copyOf(results);
      ResultImpl<T> existing = compounds.get(key);
      if (existing == null) {
        ResultImpl<T> created = new ResultImpl<>(source, key, averagePeriod, averageTimeUnit, executor, historySize, historyPeriod, historyTimeUnit);
        ResultImpl<T> racer = compounds.putIfAbsent(key, created);
        if (racer == null) {
          return created;
        } else {
          return racer;
        }
      } else {
        return existing;
      }
    }
  }

  @Override
  public CountOperation<T> asCountOperation() {
    return new CountOperationImpl<>(this);
  }

  @Override
  public SampledStatistic<Double> ratioOf(EnumSet<T> numerator, EnumSet<T> denominator) {
    @SuppressWarnings("unchecked")
    List<Set<T>> key = Arrays.asList(EnumSet.copyOf(numerator), EnumSet.copyOf(denominator));

    ExpiringSampledStatistic<Double> existing = ratios.get(key);
    if (existing == null) {
      final SampledStatistic<Double> numeratorRate = compound(numerator).rate();
      final SampledStatistic<Double> denominatorRate = compound(denominator).rate();
      ExpiringSampledStatistic<Double> created = new ExpiringSampledStatistic<>(() -> numeratorRate.value() / denominatorRate.value(), executor, historySize, historyPeriod, historyTimeUnit, StatisticType.RATIO);
      ExpiringSampledStatistic<Double> racer = ratios.putIfAbsent(key, created);
      if (racer == null) {
        return created;
      } else {
        return racer;
      }
    } else {
      return existing;
    }
  }

  @Override
  public void setAlwaysOn(boolean enable) {
    alwaysOn = enable;
    if (enable) {
      for (ResultImpl<T> op : operations.values()) {
        op.start();
      }
      for (ResultImpl<T> op : compounds.values()) {
        op.start();
      }
      for (ExpiringSampledStatistic<Double> ratio : ratios.values()) {
        ratio.start();
      }
    }
  }

  @Override
  public boolean isAlwaysOn() {
    return alwaysOn;
  }

  @Override
  public void setWindow(long time, TimeUnit unit) {
    averagePeriod = time;
    averageTimeUnit = unit;
    for (ResultImpl<T> op : operations.values()) {
      op.setWindow(averagePeriod, averageTimeUnit);
    }
    for (ResultImpl<T> op : compounds.values()) {
      op.setWindow(averagePeriod, averageTimeUnit);
    }
  }

  @Override
  public void setHistory(int samples, long time, TimeUnit unit) {
    historySize = samples;
    historyPeriod = time;
    historyTimeUnit = unit;
    for (ResultImpl<T> op : operations.values()) {
      op.setHistory(historySize, historyPeriod, historyTimeUnit);
    }
    for (ResultImpl<T> op : compounds.values()) {
      op.setHistory(historySize, historyPeriod, historyTimeUnit);
    }
    for (ExpiringSampledStatistic<Double> ratio : ratios.values()) {
      ratio.setHistory(historySize, historyPeriod, historyTimeUnit);
    }
  }

  @Override
  public long getWindowSize(TimeUnit unit) {
    return unit.convert(averagePeriod, unit);
  }

  /**
   * Get the history sample size.
   */
  @Override
  public int getHistorySampleSize() {
    return historySize;
  }

  /**
   * Get the history sample time.
   */
  @Override
  public long getHistorySampleTime(TimeUnit unit) {
    return unit.convert(historySize, unit);
  }

  /**
   * Expire.
   *
   * @param expiryTime the expiry time
   * @return true, if successful
   */
  @Override
  @SuppressFBWarnings("NS_DANGEROUS_NON_SHORT_CIRCUIT")
  public boolean expire(long expiryTime) {
    if (alwaysOn) {
      return false;
    } else {
      boolean expired = true;
      for (ResultImpl<?> o : operations.values()) {
        // Not using && on purpose here. expire() has a side-effect. We want to make sure it's called (no short-circuit evaluation)
        expired &= o.expire(expiryTime);
      }
      compounds.values().removeIf(result -> result.expire(expiryTime));
      ratios.values().removeIf(statistic -> statistic.expire(expiryTime));
      return expired && compounds.isEmpty() && ratios.isEmpty();
    }
  }

}

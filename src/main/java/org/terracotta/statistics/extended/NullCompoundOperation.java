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

import org.terracotta.statistics.archive.Timestamped;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * The Class NullCompoundOperation.
 *
 * @param <T> the generic type
 * @author cdennis
 */
public final class NullCompoundOperation<T extends Enum<T>> implements CompoundOperation<T> {

  @SuppressWarnings("rawtypes")
  private static final CompoundOperation INSTANCE = new NullCompoundOperation();

  private NullCompoundOperation() {
    //singleton
  }

  /**
   * Instance.
   *
   * @param <T> the generic type
   * @return the operation
   */
  @SuppressWarnings("unchecked")
  public static <T extends Enum<T>> CompoundOperation<T> instance(Class<T> klazz) {
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<T> type() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result component(T result) {
    return NullOperation.instance();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Result compound(Set<T> results) {
    return NullOperation.instance();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SampledStatistic<Double> ratioOf(Set<T> numerator, Set<T> denomiator) {
    return NullSampledStatistic.instance(Double.NaN);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAlwaysOn(boolean enable) {
    //no-op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWindow(long time, TimeUnit unit) {
    //no-op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setHistory(int samples, long time, TimeUnit unit) {
    //no-op
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAlwaysOn() {
    // no-op
    return false;
  }

  @Override
  public long getWindowSize(TimeUnit unit) {
    // no-op
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getHistorySampleSize() {
    // no-op
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getHistorySampleTime(TimeUnit unit) {
    // no-op
    return 0;
  }

  /**
   * Null result object
   *
   * @author cdennis
   */
  final static class NullOperation implements Result {

    private static final Result INSTANCE = new NullOperation();

    /**
     * Instantiates a new null operation.
     */
    private NullOperation() {
      //singleton
    }

    /**
     * Instance method
     *
     * @return
     */
    static final Result instance() {
      return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampledStatistic<Long> count() {
      return NullSampledStatistic.instance(0L);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampledStatistic<Double> rate() {
      return NullSampledStatistic.instance(Double.NaN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Latency latency() throws UnsupportedOperationException {
      return NullLatency.instance();
    }
  }

  /**
   * Noop latency class
   *
   * @author cdennis
   */
  final static class NullLatency implements Latency {

    private static final Latency INSTANCE = new NullLatency();

    /**
     * Private constructor
     */
    private NullLatency() {
    }

    /**
     * Instance accessor
     *
     * @return
     */
    static Latency instance() {
      return INSTANCE;
    }

    /**
     * minimum
     */
    @Override
    public SampledStatistic<Long> minimum() {
      return NullSampledStatistic.instance(null);
    }

    /**
     * maximum
     */
    @Override
    public SampledStatistic<Long> maximum() {
      return NullSampledStatistic.instance(null);
    }

    /**
     * average
     */
    @Override
    public SampledStatistic<Double> average() {
      return NullSampledStatistic.instance(Double.NaN);
    }
  }

  /**
   * Null statistic class
   *
   * @param <T>
   * @author cdennis
   */
  final static class NullSampledStatistic<T extends Number> implements SampledStatistic<T> {

    private static final Map<Object, SampledStatistic<?>> COMMON = new HashMap<Object, SampledStatistic<?>>();

    static {
      COMMON.put(Double.NaN, new NullSampledStatistic<Double>(Double.NaN));
      COMMON.put(Float.NaN, new NullSampledStatistic<Float>(Float.NaN));
      COMMON.put(Long.valueOf(0L), new NullSampledStatistic<Long>(0L));
      COMMON.put(null, new NullSampledStatistic<Long>(null));
    }

    private final T value;

    /**
     * Constructor
     *
     * @param value initial value
     */
    private NullSampledStatistic(T value) {
      this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean active() {
      return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T value() {
      return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Timestamped<T>> history() throws UnsupportedOperationException {
      return Collections.emptyList();
    }

    /**
     * instance
     *
     * @param value
     * @return
     */
    static <T extends Number> SampledStatistic<T> instance(T value) {
      @SuppressWarnings("unchecked")
      SampledStatistic<T> cached = (SampledStatistic<T>) COMMON.get(value);
      if (cached == null) {
        return new NullSampledStatistic<T>(value);
      } else {
        return cached;
      }
    }

  }
}

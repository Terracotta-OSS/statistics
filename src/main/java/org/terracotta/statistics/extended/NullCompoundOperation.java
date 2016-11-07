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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   * @param klazz concrete class of {@code <T>}
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
  public Result compound(EnumSet<T> results) {
    return NullOperation.instance();
  }

  @Override
  public CountOperation<T> asCountOperation() {
    return new CountOperation<T>() {
      @Override
      public long value(T result) {
        return -1L;
      }

      @Override
      public long value(T... results) {
        return -1L;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SampledStatistic<Double> ratioOf(EnumSet<T> numerator, EnumSet<T> denominator) {
    return NullSampledStatistic.instance(SampleType.RATIO);
  }

  @Override
  public boolean expire(long expiry) {
    return false;
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
      return NullSampledStatistic.instance(SampleType.COUNTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampledStatistic<Double> rate() {
      return NullSampledStatistic.instance(SampleType.RATE);
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
      return NullSampledStatistic.instance(SampleType.LATENCY_MIN);
    }

    /**
     * maximum
     */
    @Override
    public SampledStatistic<Long> maximum() {
      return NullSampledStatistic.instance(SampleType.LATENCY_MAX);
    }

    /**
     * average
     */
    @Override
    public SampledStatistic<Double> average() {
      return NullSampledStatistic.instance(SampleType.LATENCY_AVG);
    }
  }

  /**
   * Null statistic class
   *
   * @param <T>
   * @author cdennis
   */
  final static class NullSampledStatistic<T extends Number> implements SampledStatistic<T> {

    private static final Map<SampleType, SampledStatistic<?>> COMMON = new HashMap<SampleType, SampledStatistic<?>>();

    static {
      COMMON.put(SampleType.COUNTER, new NullSampledStatistic<Long>(0L, SampleType.COUNTER));
      COMMON.put(SampleType.RATE, new NullSampledStatistic<Double>(Double.NaN, SampleType.RATE));
      COMMON.put(SampleType.LATENCY_MIN, new NullSampledStatistic<Long>(null, SampleType.LATENCY_MIN));
      COMMON.put(SampleType.LATENCY_MAX, new NullSampledStatistic<Long>(null, SampleType.LATENCY_MAX));
      COMMON.put(SampleType.LATENCY_AVG, new NullSampledStatistic<Double>(Double.NaN, SampleType.LATENCY_AVG));
      COMMON.put(SampleType.RATIO, new NullSampledStatistic<Double>(Double.NaN, SampleType.RATIO));
      COMMON.put(SampleType.SIZE, new NullSampledStatistic<Long>(0L, SampleType.SIZE));
    }

    private final T value;
    private final SampleType type;

    /**
     * Constructor
     *
     * @param value initial value
     * @param type
     */
    private NullSampledStatistic(T value, SampleType type) {
      this.value = value;
      this.type = type;
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

    @Override
    public SampleType type() {
      return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Timestamped<T>> history() throws UnsupportedOperationException {
      return Collections.emptyList();
    }

    @Override
    public List<Timestamped<T>> history(long since) {
      return Collections.emptyList();
    }

    static <T extends Number> SampledStatistic<T> instance(SampleType type) {
      if(type == null) {
        throw new NullPointerException();
      }
      return (SampledStatistic<T>) COMMON.get(type);
    }

  }
}

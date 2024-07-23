/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou
 */
public class MemoizingValueStatistic<T extends Serializable> implements ValueStatistic<T> {

  private final long delayNs;
  private final AtomicReference<T> memoized = new AtomicReference<>();
  private final AtomicLong expiration = new AtomicLong();
  private final ValueStatistic<T> delegate;

  public MemoizingValueStatistic(long delay, TimeUnit unit, ValueStatistic<T> delegate) {
    this.delayNs = TimeUnit.NANOSECONDS.convert(delay, unit);
    this.delegate = delegate;
  }

  @Override
  public StatisticType type() {
    return delegate.type();
  }

  @Override
  public T value() {
    long now = Time.time();
    long exp = expiration.get();
    if (now >= exp && expiration.compareAndSet(exp, now + delayNs)) {
      memoized.set(delegate.value());
    }
    return memoized.get();
  }
}

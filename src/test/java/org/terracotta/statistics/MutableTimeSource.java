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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author cdennis
 */
public class MutableTimeSource implements Time.TimeSource {

  private final AtomicLong time = new AtomicLong();
  private final AtomicLong absoluteTime = new AtomicLong();

  @Override
  public long time() {
    return time.get();
  }

  public void advanceTime(long by, TimeUnit unit) {
    time.addAndGet(unit.toNanos(by));
  }

  @Override
  public long absoluteTime() {
    return time.get();
  }

  public void advanceAbsoluteTime(long by, TimeUnit unit) {
    absoluteTime.addAndGet(unit.toMillis(by));
  }
}

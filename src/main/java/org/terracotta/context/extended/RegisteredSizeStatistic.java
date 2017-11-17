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
package org.terracotta.context.extended;

import org.terracotta.statistics.extended.ExpiringSampledStatistic;
import org.terracotta.statistics.extended.SampledStatistic;
import org.terracotta.statistics.extended.SamplingSupport;

/**
 * Class used to register size statistics (BytesSize, etc.)
 */
public class RegisteredSizeStatistic implements RegisteredStatistic {

  private final ExpiringSampledStatistic<Long> sampledStatistic;

  public RegisteredSizeStatistic(ExpiringSampledStatistic<Long> sampledStatistic) {
    this.sampledStatistic = sampledStatistic;
  }

  @Override
  public RegistrationType getType() {
    return RegistrationType.GAUGE;
  }

  public SampledStatistic<Long> getSampledStatistic() {
    return sampledStatistic;
  }

  @Override
  public SamplingSupport getSupport() {
    return sampledStatistic;
  }
}

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
import org.terracotta.statistics.extended.SamplingSupport;

/**
 * Class used to register counter statistics (MappingCount, etc.)
 */
public class RegisteredCounterStatistic extends RegisteredStatistic {

  private final ExpiringSampledStatistic<?> sampledStatistic;

  public RegisteredCounterStatistic(ExpiringSampledStatistic<?> sampledStatistic) {
    this.sampledStatistic = sampledStatistic;
  }

  public ExpiringSampledStatistic<?> getSampledStatistic() {
    return sampledStatistic;
  }

  @Override
  public SamplingSupport getSupport() {
    return sampledStatistic;
  }
}

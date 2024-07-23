/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.statistics.ratio;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.EnumSet.of;

/**
 * @author Mathieu Carbou
 */
class TierOperationOutcomes {

  static final Map<GetOutcome, Set<StoreOperationOutcomes.GetOutcome>> GET_TRANSLATION;

  static {
    Map<GetOutcome, Set<StoreOperationOutcomes.GetOutcome>> translation = new EnumMap<>(GetOutcome.class);
    translation.put(GetOutcome.HIT, of(StoreOperationOutcomes.GetOutcome.HIT));
    translation.put(GetOutcome.MISS, of(StoreOperationOutcomes.GetOutcome.MISS));
    GET_TRANSLATION = unmodifiableMap(translation);
  }

  enum GetOutcome {
    HIT,
    MISS,
  }

}

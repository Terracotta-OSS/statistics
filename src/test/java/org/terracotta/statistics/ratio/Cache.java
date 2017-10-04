/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
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
package org.terracotta.statistics.ratio;

import org.terracotta.statistics.MappedOperationStatistic;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.observer.OperationObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.terracotta.statistics.StatisticBuilder.operation;

/**
 * @author Mathieu Carbou
 */
class Cache {

  OperationObserver<StoreOperationOutcomes.GetOutcome> getObserver;
  Map<String, String> data = new ConcurrentHashMap<String, String>() {
    @Override
    public String get(Object key) {
      // costly operation
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return super.get(key);
    }
  };

  Cache() {
    getObserver = operation(StoreOperationOutcomes.GetOutcome.class).named("get").of(this).tag("OnHeap").build();

    MappedOperationStatistic<StoreOperationOutcomes.GetOutcome, TierOperationOutcomes.GetOutcome> get =
        new MappedOperationStatistic<>(
            this,
            TierOperationOutcomes.GET_TRANSLATION,
            "get",
            10000,
            "get",
            "OnHeap");

    StatisticsManager.associate(get).withParent(this);
  }

  String put(String key, String value) {
    return data.put(key, value);
  }

  String get(String k) {
    getObserver.begin();
    String v = data.get(k);
    if (v == null) {
      getObserver.end(StoreOperationOutcomes.GetOutcome.MISS);
    } else {
      getObserver.end(StoreOperationOutcomes.GetOutcome.HIT);
    }
    return v;
  }

}

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
package org.terracotta.statistics.strawman;

import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.observer.OperationObserver;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<K, V> {

  private final Map<K, V> data = new ConcurrentHashMap<>();
  @ContextAttribute("name") private final String name;

  private final OperationObserver<PutResult> putObserver = StatisticsManager.createOperationStatistic(this, "put", Collections.emptySet(), PutResult.class);
  private final OperationObserver<RemoveResult> removeObserver = StatisticsManager.createOperationStatistic(this, "remove", Collections.emptySet(), RemoveResult.class);
  private final OperationObserver<GetResult> getObserver = StatisticsManager.createOperationStatistic(this, "get", Collections.emptySet(), GetResult.class);

  public Cache(String name) {
    this.name = name;
  }

  public V put(K key, V value) {
    putObserver.begin();
    V old = data.put(key, value);
    if (old == null) {
      putObserver.end(PutResult.INSERT);
    } else {
      putObserver.end(PutResult.UPDATE);
    }
    return old;
  }

  public V remove(K key) {
    removeObserver.begin();
    V removed = data.remove(key);
    if (removed == null) {
      removeObserver.end(RemoveResult.FAIL);
    } else {
      removeObserver.end(RemoveResult.SUCCESS);
    }
    return removed;
  }

  public V get(K key) {
    getObserver.begin();
    V result = data.get(key);
    if (result == null) {
      getObserver.end(GetResult.MISS);
    } else {
      getObserver.end(GetResult.HIT);
    }
    return result;
  }

}

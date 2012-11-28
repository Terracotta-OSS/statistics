/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.strawman;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.observer.OperationObserver;

import static java.util.Collections.singletonMap;

public class Cache<K, V> {
  
  private final Map<K, V> data = new ConcurrentHashMap<K, V>();
  @ContextAttribute("name") private final String name;

  private final OperationObserver<PutResult> putObserver = StatisticsManager.createOperationStatistic(this, singletonMap("name", "put"), PutResult.class);
  private final OperationObserver<RemoveResult> removeObserver = StatisticsManager.createOperationStatistic(this, singletonMap("name", "remove"), RemoveResult.class);
  private final OperationObserver<GetResult> getObserver = StatisticsManager.createOperationStatistic(this, singletonMap("name", "get"), GetResult.class);
  
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

  enum GetResult {
    HIT, MISS;
  }
  
  enum PutResult {
    INSERT, UPDATE;
  }
  
  enum RemoveResult {
    SUCCESS, FAIL;
  }
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.statistics.strawman;

import java.util.ArrayList;
import java.util.Collection;

import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.context.annotations.ContextChild;

import static org.terracotta.statistics.StatisticsManager.associate;
import static org.terracotta.statistics.StatisticsManager.dissociate;

public class CacheManager {
  
  @ContextAttribute("name") private final String name;
  
  private final Collection<Cache> caches = new ArrayList<Cache>();
  
  public CacheManager(String name) {
    this.name = name;
  }
  
  public void addCache(Cache cache) {
    caches.add(cache);
    associate(this).withChild(cache);
  }
  
  public void removeCache(Cache cache) {
    caches.remove(cache);
    dissociate(this).fromChild(cache);
  }
}

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
package org.terracotta.statistics.strawman;

import org.terracotta.context.annotations.ContextAttribute;

import java.util.ArrayList;
import java.util.Collection;

import static org.terracotta.statistics.StatisticsManager.associate;
import static org.terracotta.statistics.StatisticsManager.dissociate;

public class CacheManager {

  @ContextAttribute("name") private final String name;

  private final Collection<Cache<?, ?>> caches = new ArrayList<>();

  public CacheManager(String name) {
    this.name = name;
  }

  public void addCache(Cache<?, ?> cache) {
    caches.add(cache);
    associate(this).withChild(cache);
  }

  public void removeCache(Cache<?, ?> cache) {
    caches.remove(cache);
    dissociate(this).fromChild(cache);
  }
}

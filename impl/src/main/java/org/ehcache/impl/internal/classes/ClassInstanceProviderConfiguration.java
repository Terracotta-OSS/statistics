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

package org.ehcache.impl.internal.classes;

import org.ehcache.impl.internal.classes.ClassInstanceConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for ProviderFactory config that instantiates service classes.
 * Keeps the order in which defaults are added.
 *
 * @author Alex Snaps
 */
public class ClassInstanceProviderConfiguration<K, T> {

  private Map<K, ClassInstanceConfiguration<T>> defaults = new LinkedHashMap<K, ClassInstanceConfiguration<T>>();

  public Map<K, ClassInstanceConfiguration<T>> getDefaults() {
    return defaults;
  }

}

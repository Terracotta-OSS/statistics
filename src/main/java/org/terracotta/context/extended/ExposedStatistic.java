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

import org.terracotta.context.annotations.ContextAttribute;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Ludovic Orban
 */
public class ExposedStatistic {

  @ContextAttribute("name") private final String name;
  @ContextAttribute("type") private final Class<?> type;
  @ContextAttribute("tags") private final Set<String> tags;
  @ContextAttribute("properties") private final Map<String, Object> properties;
  @ContextAttribute("this") private final Object stat;

  public ExposedStatistic(String name, Class<?> type, Set<String> tags, Map<String, Object> properties, Object stat) {
    this.name = name;
    this.type = type;
    this.tags = tags;
    this.properties = (properties == null ? Collections.<String, Object>emptyMap() : properties);
    this.stat = stat;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public Set<String> getTags() {
    return tags;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public Object getStat() {
    return stat;
  }
}

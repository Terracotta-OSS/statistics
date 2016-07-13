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

import java.util.Set;

/**
 * @author Ludovic Orban
 */
public final class OperationStatisticDescriptor {

  private final String observerName;
  private final Set<String> tags;
  private final Class<? extends Enum<?>> type;

  private OperationStatisticDescriptor(String observerName, Set<String> tags, Class<? extends Enum<?>> type) {
    this.observerName = observerName;
    this.tags = tags;
    this.type = type;
  }

  public String getObserverName() {
    return observerName;
  }

  public Set<String> getTags() {
    return tags;
  }

  public Class<? extends Enum<?>> getType() {
    return type;
  }

  public static OperationStatisticDescriptor descriptor(String observerName, Set<String> tags, Class<? extends Enum<?>> type) {
    return new OperationStatisticDescriptor(observerName, tags, type);
  }

}

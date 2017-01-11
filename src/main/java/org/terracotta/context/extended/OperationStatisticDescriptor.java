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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ludovic Orban
 */
public final class OperationStatisticDescriptor<T extends Enum<T>> {

  private final String observerName;
  private final Set<String> tags;
  private final Class<T> type;

  private OperationStatisticDescriptor(String observerName, Set<String> tags, Class<T> type) {
    this.observerName = observerName;
    this.tags = Collections.unmodifiableSet(tags);
    this.type = type;
  }

  public String getObserverName() {
    return observerName;
  }

  public Set<String> getTags() {
    return tags;
  }

  public Class<T> getType() {
    return type;
  }

  public static <T extends Enum<T>> OperationStatisticDescriptor<T> descriptor(String observerName, Set<String> tags, Class<T> type) {
    return new OperationStatisticDescriptor<T>(observerName, tags, type);
  }

  public static <T extends Enum<T>> OperationStatisticDescriptor<T> descriptor(String observerName, Class<T> type, String... tags) {
    return new OperationStatisticDescriptor<T>(observerName, new HashSet<String>(Arrays.asList(tags)), type);
  }

}

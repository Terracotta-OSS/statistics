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
package org.terracotta.statistics.extended;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author Ludovic Orban
 */
class CountOperationImpl<T extends Enum<T>> implements CountOperation<T> {

  private final CompoundOperation<T> compoundOperation;

  CountOperationImpl(CompoundOperation<T> compoundOperation) {
    this.compoundOperation = compoundOperation;
  }

  @Override
  public long value(T result) {
    return compoundOperation.component(result).count().value();
  }

  @Override
  public long value(T... results) {
    return compoundOperation.compound(EnumSet.copyOf(Arrays.asList(results))).count().value();
  }
}

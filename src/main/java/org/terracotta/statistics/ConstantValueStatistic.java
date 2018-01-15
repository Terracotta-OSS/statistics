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
package org.terracotta.statistics;

import java.io.Serializable;

/**
 * @author cdennis
 */
public class ConstantValueStatistic<T extends Serializable> implements ValueStatistic<T>, Serializable {

  private static final long serialVersionUID = 1L;
  private final T value;
  private final StatisticType type;

  public ConstantValueStatistic(StatisticType type, T value) {
    this.value = value;
    this.type = type;
  }

  @Override
  public T value() {
    return value;
  }

  @Override
  public StatisticType type() {
    return type;
  }
}

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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author cdennis
 */
public class ConstantValueStatistic<T extends Number> implements ValueStatistic<T> {

  private static final Map<Object, ValueStatistic<?>> common = new HashMap<Object, ValueStatistic<?>>();
  static {
    common.put(Integer.valueOf(0), new ConstantValueStatistic<Integer>(0));
    common.put(Long.valueOf(0L), new ConstantValueStatistic<Long>(0L));
    common.put(null, new ConstantValueStatistic(null));
  }
  
  public static <T extends Number> ValueStatistic<T> instance(T value) {
    ValueStatistic<T> interned = (ValueStatistic<T>) common.get(value);
    if (interned == null) {
      return new ConstantValueStatistic<T>(value);
    } else {
      return interned;
    }
  }
  
  private final T value;
  
  private ConstantValueStatistic(T value) {
    this.value = value;
  }
  
  @Override
  public T value() {
    return value;
  }
}

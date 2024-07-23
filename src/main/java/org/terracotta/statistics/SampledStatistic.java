/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.statistics;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ludovic Orban
 */
public interface SampledStatistic<T extends Serializable> extends ValueStatistic<T> {

  /**
   * The history of values
   *
   * @return the list
   */
  List<Sample<T>> history();

  /**
   * The history of values, since a given time in ms
   *
   * @param since starting point of history in ms
   * @return the list
   */
  List<Sample<T>> history(long since);

}

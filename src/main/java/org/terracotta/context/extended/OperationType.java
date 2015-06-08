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

import org.terracotta.context.query.Query;

import java.util.Set;

/**
 * @author Ludovic Orban
 */
public interface OperationType {

  /**
   * If this statistic is required.
   * <p>
   * If required and this statistic is not present an exception will be thrown.
   *
   * @return true if required
   */
  boolean required();

  /**
   * Query that select context nodes for this statistic.
   *
   * @return context query
   */
  Query context();

  /**
   * Operation result type.
   *
   * @return operation result type
   */
  Class<? extends Enum<?>> type();

  /**
   * The name of the statistic as found in the statistics context tree.
   *
   * @return the statistic name
   */
  String operationName();

  /**
   * A set of tags that will be on the statistic found in the statistics context tree.
   *
   * @return the statistic tags
   */
  Set<String> tags();

}

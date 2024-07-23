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
package org.terracotta.context.query;

import static org.terracotta.context.query.QueryBuilder.queryBuilder;

/**
 * @author Ludovic Orban
 */
public abstract class Queries {

  private Queries() {
  }

  /**
   * Creates a query selecting self.
   *
   * @return children query
   */
  public static Query self() {
    return queryBuilder().build();
  }

  /**
   * Creates a query selecting all children.
   *
   * @return children query
   */
  public static Query children() {
    return queryBuilder().children().build();
  }

  /**
   * Creates a query selecting all descendants.
   *
   * @return descendants query
   */
  public static Query descendants() {
    return queryBuilder().descendants().build();
  }

}

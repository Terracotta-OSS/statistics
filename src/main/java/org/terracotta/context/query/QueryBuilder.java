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
package org.terracotta.context.query;

import org.terracotta.context.TreeNode;

/**
 * A {@code QueryBuilder} allows for modular assembly of context graph queries.
 * <p>
 * Query assembly is performed by chaining a sequence of graph traversal and
 * filtering operations together in order to select a particular set of the 
 * input node set's descendants.
 */
public class QueryBuilder {

  private Query current;
  
  private QueryBuilder() {
    current = NullQuery.INSTANCE;
  }
  
  /**
   * Creates a new query builder instance.
   * <p>
   * A newly constructed query builder represents the identity query.  It simply
   * returns the input node set as the output node set.
   * 
   * @return a new query builder
   */
  public static QueryBuilder queryBuilder() {
    return new QueryBuilder();
  }
  
  /**
   * Filters the current node set using the supplied {@code Matcher}.
   * <p>
   * Nodes in the current node set that are not selected by the supplied matcher
   * are removed.
   * 
   * @param filter matcher to apply
   * @return this query builder
   */
  public QueryBuilder filter(Matcher<? super TreeNode> filter) {
    return chain(new Filter(filter));
  }
  
  /**
   * Selects the union of the current node sets child nodes.
   * 
   * @return this query builder
   */
  public QueryBuilder children() {
    return chain(Children.INSTANCE);
  }
  
  /**
   * Selects the merged descendant set of the current node set.
   * <p>
   * More precisely this recursively merges the children of each member of the
   * node-set in to the output node set until the set ceases to grow.
   * 
   * @return this query builder
   */
  public QueryBuilder descendants() {
    return chain(Descendants.INSTANCE);
  }
  
  /**
   * Applies the given query on the currently selected node set.
   * 
   * @param query query to apply
   * @return this query builder
   */
  public QueryBuilder chain(Query query) {
    current = new ChainedQuery(current, query);
    return this;
  }

  /**
   * Asserts that the current node set is a singleton.
   * <p>
   * If the current node set is not of size 1 then the query will terminate with
   * an {@code IllegalStateException}.
   * 
   * @return this query builder
   */
  public QueryBuilder ensureUnique() {
    return chain(EnsureUnique.INSTANCE);
  }

  /**
   * Selects an empty node set.
   * 
   * @return this query builder
   */
  public QueryBuilder empty() {
    current = EmptyQuery.INSTANCE;
    return this;
  }
  
  /**
   * Returns a query that represents the currently assembled transformation.
   * 
   * @return a newly constructed query
   */
  public Query build() {
    return current;
  }
}

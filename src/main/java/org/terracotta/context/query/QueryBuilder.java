/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import org.terracotta.context.TreeNode;

public class QueryBuilder {

  private Query current;
  
  private QueryBuilder() {
    current = NullQuery.INSTANCE;
  }
  
  public static QueryBuilder queryBuilder() {
    return new QueryBuilder();
  }
  
  public QueryBuilder filter(Matcher<? super TreeNode<?, ?, ?>> filter) {
    return chain(new Filter(filter));
  }
  
  public QueryBuilder children() {
    return chain(Children.INSTANCE);
  }
  
  public QueryBuilder descendants() {
    return chain(Descendants.INSTANCE);
  }
  
  public QueryBuilder chain(Query query) {
    current = new ChainedQuery(current, query);
    return this;
  }

  public QueryBuilder ensureUnique() {
    return chain(EnsureUnique.INSTANCE);
  }
  
  public Query build() {
    return current;
  }
}

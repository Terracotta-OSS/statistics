/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Collection;

import org.terracotta.context.TreeNode;

class ChainedQuery implements Query {

  private final Query current;
  private final Query previous;
  
  public ChainedQuery(Query previous, Query current) {
    this.previous = previous;
    this.current = current;
  }

  @Override
  public final <I, K, V> Collection<? extends TreeNode<I, K, V>> execute(Collection<? extends TreeNode<I, K, V>> input) {
    return current.execute(previous.execute(input));
  }

  @Override
  public String toString() {
    return previous + " => " + current;
  }
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Collection;

import org.terracotta.context.TreeNode;

class NullQuery implements Query {

  static final Query INSTANCE = new NullQuery();
  
  private NullQuery() {
    //singleton
  }
  
  @Override
  public <I, K, V> Collection<? extends TreeNode<I, K, V>> execute(Collection<? extends TreeNode<I, K, V>> input) {
    return input;
  }
  
  @Override
  public String toString() {
    return "";
  }
}

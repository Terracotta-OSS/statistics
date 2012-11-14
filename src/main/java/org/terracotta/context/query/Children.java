/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.terracotta.context.TreeNode;

class Children<I, K, V> implements Query {

  static final Query INSTANCE = new Children();
  
  @Override
  public <I, K, V> Collection<TreeNode<I, K, V>> execute(Collection<? extends TreeNode<I, K, V>> input) {
    Set<TreeNode<I, K, V>> output = new HashSet<TreeNode<I, K, V>>();
    for (TreeNode<I, K, V> node : input) {
      output.addAll(node.getChildren());
    }
    return output;
  }
  
  @Override
  public String toString() {
    return "children";
  }
}

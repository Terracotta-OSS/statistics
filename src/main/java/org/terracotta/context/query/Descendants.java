/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.terracotta.context.TreeNode;

class Descendants<I, K, V> implements Query {

  static final Query INSTANCE = new Descendants();

  @Override
  public <I, K, V> Set<TreeNode<I, K, V>> execute(Set<TreeNode<I, K, V>> input) {
    Set<TreeNode<I, K, V>> descendants = new HashSet<TreeNode<I, K, V>>();
    for (Set<TreeNode<I, K, V>> children = Children.INSTANCE.execute(input); !children.isEmpty(); children = Children.INSTANCE.execute(children)) {
      if (!descendants.addAll(children)) {
        break;
      }
    }
    return descendants;
  }
  
  @Override
  public String toString() {
    return "descendants";
  }
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Collection;
import java.util.HashSet;

import org.terracotta.context.TreeNode;

class Descendants<I, K, V> implements Query {

  static final Query INSTANCE = new Descendants();

  @Override
  public <I, K, V> Collection<TreeNode<I, K, V>> execute(Collection<? extends TreeNode<I, K, V>> input) {
    Collection<TreeNode<I, K, V>> descendants = new HashSet<TreeNode<I, K, V>>();
    for (Collection<? extends TreeNode<I, K, V>> children = Children.INSTANCE.execute(input); !children.isEmpty(); children = Children.INSTANCE.execute(children)) {
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

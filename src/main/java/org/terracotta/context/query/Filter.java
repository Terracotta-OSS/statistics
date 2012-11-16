/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.terracotta.context.TreeNode;

class Filter implements Query {

  private final Matcher<? super TreeNode<?, ?, ?>> filter;
  
  public Filter(Matcher<? super TreeNode<?, ?, ?>> filter) {
    if (filter == null) {
      throw new NullPointerException("Cannot filter using a null matcher");
    } else {
      this.filter = filter;
    }
  }
  
  @Override
  public <I, K, V> Set<TreeNode<I, K, V>> execute(Set<TreeNode<I, K, V>> input) {
    Set<TreeNode<I, K, V>> output = new HashSet<TreeNode<I, K, V>>(input);
    for (Iterator<TreeNode<I, K, V>> it = output.iterator(); it.hasNext(); ) {
      if (!filter.matches(it.next())) {
        it.remove();
      }
    }
    return output;
  }

  @Override
  public String toString() {
    return "filter for nodes with " + filter;
  }
}

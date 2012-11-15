/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.terracotta.context.TreeNode;

class Filter implements Query {

  private final Matcher<? super TreeNode<?, ?, ?>> filter;
  
  public Filter(Matcher<? super TreeNode<?, ?, ?>> filter) {
    this.filter = filter;
  }
  
  @Override
  public <I, K, V> Collection<TreeNode<I, K, V>> execute(Collection<? extends TreeNode<I, K, V>> input) {
    Collection<TreeNode<I, K, V>> output = new ArrayList<TreeNode<I, K, V>>(input);
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

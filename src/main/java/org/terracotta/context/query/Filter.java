/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.terracotta.context.TreeNode;

class Filter implements Query {

  private final Matcher<? super TreeNode> filter;
  
  public Filter(Matcher<? super TreeNode> filter) {
    if (filter == null) {
      throw new NullPointerException("Cannot filter using a null matcher");
    } else {
      this.filter = filter;
    }
  }
  
  @Override
  public Set<TreeNode> execute(Set<TreeNode> input) {
    Set<TreeNode> output = new HashSet<TreeNode>(input);
    for (Iterator<TreeNode> it = output.iterator(); it.hasNext(); ) {
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

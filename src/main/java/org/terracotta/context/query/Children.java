/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.HashSet;
import java.util.Set;

import org.terracotta.context.TreeNode;

class Children implements Query {

  static final Query INSTANCE = new Children();
  
  @Override
  public Set<TreeNode> execute(Set<TreeNode> input) {
    Set<TreeNode> output = new HashSet<TreeNode>();
    for (TreeNode node : input) {
      output.addAll(node.getChildren());
    }
    return output;
  }
  
  @Override
  public String toString() {
    return "children";
  }
}

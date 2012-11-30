/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.HashSet;
import java.util.Set;

import org.terracotta.context.TreeNode;

class Descendants implements Query {

  static final Query INSTANCE = new Descendants();

  @Override
  public Set<TreeNode> execute(Set<TreeNode> input) {
    Set<TreeNode> descendants = new HashSet<TreeNode>();
    for (Set<TreeNode> children = Children.INSTANCE.execute(input); !children.isEmpty(); children = Children.INSTANCE.execute(children)) {
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

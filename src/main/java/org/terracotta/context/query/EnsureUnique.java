/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Set;

import org.terracotta.context.TreeNode;

class EnsureUnique implements Query {
  
  static Query INSTANCE = new EnsureUnique();
  
  private EnsureUnique() {
    //singleton
  }

  @Override
  public Set<TreeNode> execute(Set<TreeNode> input) {
    if (input.size() == 1) {
      return input;
    } else {
      throw new IllegalStateException("Expected a uniquely identified node: found " + input.size());
    }
  }
}

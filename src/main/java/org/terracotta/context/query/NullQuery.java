/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Set;

import org.terracotta.context.TreeNode;

class NullQuery implements Query {

  static final Query INSTANCE = new NullQuery();
  
  private NullQuery() {
    //singleton
  }
  
  @Override
  public Set<TreeNode> execute(Set<TreeNode> input) {
    return input;
  }
  
  @Override
  public String toString() {
    return "";
  }
}

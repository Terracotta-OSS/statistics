/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Set;

import org.terracotta.context.TreeNode;

public interface Query {

  Set<TreeNode> execute(Set<TreeNode> input); 
}

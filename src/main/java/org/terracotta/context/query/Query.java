/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Set;

import org.terracotta.context.TreeNode;

/**
 * A {@code Query} instance transforms an input node set into an output node set.
 * <p>
 * Useful implementations will normally perform a sequence of graph traversal
 * and node filtering operations to generate the query result.
 */
public interface Query {

  /**
   * Transforms the {@code input} node set in to an output node set.
   * 
   * @param input query input node set
   * @return the output node set
   */
  Set<TreeNode> execute(Set<TreeNode> input); 
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

public interface ContextListener<I, K, V> {
  
  void graphAdded(TreeNode<I, K, V> parent, TreeNode<I, K, V> added);
  
  void graphRemoved(TreeNode<I, K, V> parent, TreeNode<I, K, V> removed);
}

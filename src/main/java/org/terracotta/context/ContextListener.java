/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

public interface ContextListener {
  
  void graphAdded(TreeNode parent, TreeNode added);
  
  void graphRemoved(TreeNode parent, TreeNode removed);
}

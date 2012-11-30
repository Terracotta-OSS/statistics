/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

/**
 * The context graph mutation listener interface.
 */
public interface ContextListener {
  
  /**
   * Called when a new subgraph is attached.
   * <p>
   * The {@code parent} node is the currently attached node to which the
   * incoming graph has been attached.  The {@code added} node is the newly
   * attached sub-graph.  The graph accessible beneath {@code added} may include
   * sub-graphs that are already attached to this tree.
   * 
   * @param parent parent of the new sub-graph
   * @param added newly added sub-graph
   */
  void graphAdded(TreeNode parent, TreeNode added);

  /**
   * Called when a subgraph is detached.
   * <p>
   * The {@code parent} node the still attached node from which the outgoing
   * graph has been detached.  The {@code removed} node is the just detached
   * sub-graph.  The graph accessible beneath {@code removed} may include
   * sub-graphs that are still attached to this tree.
   * 
   * @param parent previous parent of the removed sub-graph
   * @param removed the removed sub-graph
   */
  void graphRemoved(TreeNode parent, TreeNode removed);
}

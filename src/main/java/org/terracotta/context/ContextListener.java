/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

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

import org.terracotta.statistics.StatisticsManager;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Node used to wrap real tree node to keep the context object. Only used
 * by {@link ContextManager#nodeFor(Object)} to allow the fluent interface
 * to forward the context to {@link TreeNode} methods.
 * <p>
 * Currently, the only method using it is {@link ContextAwareTreeNode#clean()}
 * that will make sure the context is removed from the {@link org.terracotta.statistics.PassThroughStatistic}
 */
class ContextAwareTreeNode implements TreeNode {

  private final Object context;
  private final TreeNode wrappedNode;

  public ContextAwareTreeNode(TreeNode node, Object context) {
    this.context = context;
    this.wrappedNode = node;
  }

  @Override
  public Set<? extends TreeNode> getChildren() {
    return wrappedNode.getChildren();
  }

  @Override
  public List<? extends TreeNode> getPath() throws IllegalStateException {
    return wrappedNode.getPath();
  }

  @Override
  public Collection<List<? extends TreeNode>> getPaths() {
    return wrappedNode.getPaths();
  }

  @Override
  public ContextElement getContext() {
    return wrappedNode.getContext();
  }

  @Override
  public String toTreeString() {
    return wrappedNode.toTreeString();
  }

  @Override
  public void clean() {
    wrappedNode.clean();
    StatisticsManager.removePassThroughStatistics(context);
  }
}

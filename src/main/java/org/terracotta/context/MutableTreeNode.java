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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

class MutableTreeNode extends AbstractTreeNode {

  private final CopyOnWriteArraySet<AbstractTreeNode> parents = new CopyOnWriteArraySet<AbstractTreeNode>();
  private final ContextElement context;
  
  public MutableTreeNode(ContextElement context) {
    this.context = context;
  }

  @Override
  public ContextElement getContext() {
    return context;
  }
  
  @Override
  public String toString() {
    return "{" + context + "}";
  }

  @Override
  Set<AbstractTreeNode> getAncestors() {
    Set<AbstractTreeNode> ancestors = Collections.newSetFromMap(new IdentityHashMap<AbstractTreeNode, Boolean>());
    ancestors.addAll(parents);
    for (AbstractTreeNode parent : parents) {
      ancestors.addAll(parent.getAncestors());
    }
    return Collections.unmodifiableSet(ancestors);
  }

  @Override
  public Collection<ContextListener> getListeners() {
    return Collections.emptyList();
  }

  @Override
  void addedParent(AbstractTreeNode parent) {
    parents.add(parent);
  }

  @Override
  void removedParent(AbstractTreeNode parent) {
    parents.remove(parent);
  }

  @Override
  public Collection<List<? extends TreeNode>> getPaths() {
    Collection<List<? extends TreeNode>> paths = new ArrayList<List<? extends TreeNode>>();
    
    for (TreeNode node : parents) {
      for (List<? extends TreeNode> path : node.getPaths()) {
        List<TreeNode> newPath = new ArrayList<TreeNode>(path);
        newPath.add(this);
        paths.add(newPath);
      }
    }
    return paths;
  }
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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

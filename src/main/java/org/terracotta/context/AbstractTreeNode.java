/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

abstract class AbstractTreeNode implements TreeNode {

  private final CopyOnWriteArraySet<TreeNode> children = new CopyOnWriteArraySet<TreeNode>();
  
  public boolean addChild(AbstractTreeNode child) {
    synchronized (this) {
      Collection<AbstractTreeNode> ancestors = new HashSet<AbstractTreeNode>(getAncestors());
      ancestors.removeAll(child.getAncestors());
      if (children.add(child)) {
        child.addedParent(this);
        for (AbstractTreeNode ancestor : ancestors) {
          for (ContextListener listener : ancestor.getListeners()) {
            listener.graphAdded(this, child);
          }
        }
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean removeChild(AbstractTreeNode child) {
    synchronized (this) {
      if (children.remove(child)) {
        child.removedParent(this);
        Collection<AbstractTreeNode> ancestors = new HashSet<AbstractTreeNode>(getAncestors());
        ancestors.removeAll(child.getAncestors());
        for (AbstractTreeNode ancestor : ancestors) {
          for (ContextListener listener : ancestor.getListeners()) {
            listener.graphRemoved(this, child);
          }
        }
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public Set<? extends TreeNode> getChildren() {
    return Collections.unmodifiableSet(children);
  }

  @Override
  public List<? extends TreeNode> getPath() {
    Collection<List<? extends TreeNode>> paths = getPaths();
    if (paths.size() == 1) {
      return paths.iterator().next();
    } else {
      throw new IllegalStateException("No unique path to root");
    }
  }

  abstract void addedParent(AbstractTreeNode child);
  
  abstract void removedParent(AbstractTreeNode child);
  
  abstract Set<AbstractTreeNode> getAncestors();

  abstract Collection<ContextListener> getListeners();
}

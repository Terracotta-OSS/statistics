/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

abstract class AbstractTreeNode<I, K, V> implements TreeNode<I, K, V> {

  private final CopyOnWriteArraySet<TreeNode<I, K, V>> children = new CopyOnWriteArraySet<TreeNode<I, K, V>>();
  
  public boolean addChild(AbstractTreeNode<I, K, V> child) {
    synchronized (this) {
      Collection<AbstractTreeNode<I, K, V>> ancestors = new HashSet<AbstractTreeNode<I, K, V>>(getAncestors());
      ancestors.removeAll(child.getAncestors());
      if (children.add(child)) {
        child.addedParent(this);
        for (AbstractTreeNode<I, K, V> ancestor : ancestors) {
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

  public boolean removeChild(AbstractTreeNode<I, K, V> child) {
    synchronized (this) {
      if (children.remove(child)) {
        child.removedParent(this);
        Collection<AbstractTreeNode<I, K, V>> ancestors = new HashSet<AbstractTreeNode<I, K, V>>(getAncestors());
        ancestors.removeAll(child.getAncestors());
        for (AbstractTreeNode<I, K, V> ancestor : ancestors) {
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
  public Set<? extends TreeNode<I, K, V>> getChildren() {
    return Collections.unmodifiableSet(children);
  }

  abstract void addedParent(AbstractTreeNode<I, K, V> child);
  
  abstract void removedParent(AbstractTreeNode<I, K, V> child);
  
  abstract Set<AbstractTreeNode<I, K, V>> getAncestors();

  abstract Collection<ContextListener> getListeners();
}

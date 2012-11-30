/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class RootNode extends AbstractTreeNode {

  private final Collection<ContextListener> listeners = new CopyOnWriteArrayList<ContextListener>();
  
  @Override
  void addedParent(AbstractTreeNode child) {
    throw new IllegalStateException();
  }

  @Override
  void removedParent(AbstractTreeNode child) {
    throw new IllegalStateException();
  }

  @Override
  Set<AbstractTreeNode> getAncestors() {
    return Collections.emptySet();
  }

  @Override
  Collection<ContextListener> getListeners() {
    return Collections.unmodifiableCollection(listeners);
  }
  
  public void addListener(ContextListener listener) {
    listeners.add(listener);
  }
  
  public void removeListener(ContextListener listener) {
    listeners.remove(listener);
  }

  @Override
  public ContextElement getContext() {
    throw new IllegalStateException();
  }

  @Override
  public Collection<List<? extends TreeNode>> getPaths() {
    return Collections.<List<? extends TreeNode>>singleton(Collections.<TreeNode>emptyList());
  }
}

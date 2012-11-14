/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

class RootNode<I, K, V> extends AbstractTreeNode<I, K, V> {

  private final Collection<ContextListener> listeners = new CopyOnWriteArrayList<ContextListener>();
  
  @Override
  void addedParent(AbstractTreeNode<I, K, V> child) {
    throw new IllegalStateException();
  }

  @Override
  void removedParent(AbstractTreeNode<I, K, V> child) {
    throw new IllegalStateException();
  }

  @Override
  Set<AbstractTreeNode<I, K, V>> getAncestors() {
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
  public ContextElement<I, K, V> getContext() {
    throw new IllegalStateException();
  }
}

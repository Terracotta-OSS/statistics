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

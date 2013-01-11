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

import java.util.Arrays;
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

  @Override
  public String toTreeString() {
    return dumpSubtree(0, this);
  }
  
  public static String dumpSubtree(int indent, TreeNode node) {
    char[] indentChars = new char[indent];
    Arrays.fill(indentChars, ' ');
    StringBuilder sb = new StringBuilder();
    String nodeString = node.toString();
    sb.append(indentChars).append(nodeString).append("\n");
    for (TreeNode child : node.getChildren()) {
      sb.append(dumpSubtree(indent + 2, child));
    }
    return sb.toString();
  }
  
  abstract void addedParent(AbstractTreeNode child);
  
  abstract void removedParent(AbstractTreeNode child);
  
  abstract Set<AbstractTreeNode> getAncestors();

  abstract Collection<ContextListener> getListeners();

}

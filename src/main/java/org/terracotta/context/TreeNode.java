/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TreeNode<I, K, V> {
  
  Set<? extends TreeNode<I, K, V>> getChildren();
  
  List<? extends TreeNode<I, K, V>> getPath();
  
  Collection<List<? extends TreeNode<I, K, V>>> getPaths();
  
  ContextElement<I, K, V> getContext();
}

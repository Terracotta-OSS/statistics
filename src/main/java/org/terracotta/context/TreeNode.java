/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Set;

public interface TreeNode<I, K, V> {
  
  Set<? extends TreeNode<I, K, V>> getChildren();
  
  ContextElement<I, K, V> getContext();
}

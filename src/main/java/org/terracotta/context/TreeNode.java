/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TreeNode {
  
  Set<? extends TreeNode> getChildren();
  
  List<? extends TreeNode> getPath();
  
  Collection<List<? extends TreeNode>> getPaths();
  
  ContextElement getContext();
}

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
import java.util.List;
import java.util.Set;

/**
 * A context tree node associated with a Java object.
 * <p>
 * A {@code TreeNode} allows access to both the context information associated
 * with a Java object and to information regarding the contexts position within
 * the overall context tree or trees.
 */
public interface TreeNode {
  
  /**
   * Returns the immediate children of this context.
   * 
   * @return the context children
   */
  Set<? extends TreeNode> getChildren();
  
  /**
   * Returns the unique rooting path of this context element.
   * <p>
   * If this context element is not connected to a root in any ContextManager
   * instance or is rooted via multiple distinct paths then an
   * {@code IllegalStateException} will be thrown.
   * 
   * @return the unique rooting path
   * @throws IllegalStateException if the context is not uniquely rooted
   */
  List<? extends TreeNode> getPath() throws IllegalStateException;

  /**
   * Returns the complete set of rooting paths for this context element.
   * 
   * @return the set of rooting paths
   */
  Collection<List<? extends TreeNode>> getPaths();

  /**
   * Returns the context information associated with this node.
   * 
   * @return node context information
   */
  ContextElement getContext();
  
  String toTreeString();
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Collection;

import org.terracotta.context.TreeNode;

public interface Query {

  <I, K, V> Collection<TreeNode<I, K, V>> execute(Collection<TreeNode<I, K, V>> input); 
}

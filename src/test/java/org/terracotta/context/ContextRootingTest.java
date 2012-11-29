/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.terracotta.context.ContextTestUtils.NoAnnotations;

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.core.IsSame.*;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.query.QueryBuilder.*;

/**
 *
 * @author cdennis
 */
public class ContextRootingTest {
  
  @Test
  public void testRootCreation() {
    ContextManager manager = new ContextManager();
    
    Object root = new NoAnnotations();
    manager.root(root);
    
    Collection<TreeNode<Class, String, Object>> roots = manager.query(queryBuilder().children().build());

    assertThat(roots, hasSize(1));
    assertThat(roots.iterator().next().getContext().attributes().get("this"), sameInstance(root));
  }
  
  @Test
  public void testRootRemoval() {
    ContextManager manager = new ContextManager();
    
    Object root = new NoAnnotations();
    manager.root(root);
    manager.uproot(root);
    
    Collection<TreeNode<Class, String, Object>> roots = manager.query(queryBuilder().children().build());

    assertThat(roots, IsEmptyCollection.<TreeNode<Class, String, Object>>empty());
  }
  
  @Test
  public void testDoubleRootCreation() {
    ContextManager manager = new ContextManager();
    
    Object root = new NoAnnotations();
    manager.root(root);
    manager.root(root);
    
    Collection<TreeNode<Class, String, Object>> roots = manager.query(queryBuilder().children().build());

    assertThat(roots, hasSize(1));
    assertThat(roots.iterator().next().getContext().attributes().get("this"), sameInstance(root));
  }

  @Test
  public void testDoubleRootRemoval() {
    ContextManager manager = new ContextManager();
    
    Object root = new NoAnnotations();
    manager.root(root);
    manager.root(root);
    manager.uproot(root);
    
    Collection<TreeNode<Class, String, Object>> roots = manager.query(queryBuilder().children().build());

    assertThat(roots, IsEmptyCollection.<TreeNode<Class, String, Object>>empty());
  }
}

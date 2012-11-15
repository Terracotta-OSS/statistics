/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.terracotta.context.annotations.ContextAttribute;

import static org.hamcrest.core.IsCollectionContaining.*;
import static org.hamcrest.core.IsSame.*;
import static org.terracotta.context.ContextManager.*;
import static org.terracotta.context.query.Matchers.*;
import static org.terracotta.context.query.QueryBuilder.*;

public class ManualContextAssociationTest {
  
  @Test
  public void testAddChildAssociation() {
    Object parent = new A();
    Object child = new A();
    
    associate(parent).withChild(child);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);

    validateAssociation(manager, parent, child);
  }
  
  @Test
  public void testAddParentAssociation() {
    Object parent = new A();
    Object child = new A();
    
    associate(child).withParent(parent);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }

  @Test
  public void testRemoveChildAssociation() {
    Object parent = new A();
    Object child = new A();
    
    associate(parent).withChild(child);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    dissociate(parent).fromChild(child);

    validateNoAssociation(manager, parent, child);
  }
  
  @Test
  public void testRemoveParentAssociation() {
    Object parent = new A();
    Object child = new A();
    
    associate(child).withParent(parent);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    dissociate(child).fromParent(parent);

    validateNoAssociation(manager, parent, child);
  }

  private static void validateAssociation(ContextManager manager, Object parent, Object child) {
    TreeNode<?, ?, ?> parentNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", parent)))).build());
    TreeNode<?, ?, ?> childNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", child)))).build());
    Assert.assertThat((Collection<TreeNode<?, ?, ?>>) parentNode.getChildren(), hasItem(sameInstance(childNode)));
  }
  
  private static void validateNoAssociation(ContextManager manager, Object parent, Object child) {
    TreeNode<?, ?, ?> parentNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", parent)))).build());
    Assert.assertTrue(queryBuilder().children().filter(context(attributes(hasAttribute("this", child)))).build().execute(Collections.singleton(parentNode)).isEmpty());
  }
  
  @ContextAttribute("this") static class A {}
}

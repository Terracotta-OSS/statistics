/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import org.junit.Test;
import org.terracotta.context.annotations.ContextAttribute;

import static org.terracotta.context.ContextAssociationTestUtil.*;
import static org.terracotta.context.ContextManager.*;

public class ManualContextAssociationTest {
  
  @Test
  public void testAddChildAssociation() {
    Object parent = new NoAnnotations();
    Object child = new NoAnnotations();
    
    associate(parent).withChild(child);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);

    validateAssociation(manager, parent, child);
  }
  
  @Test
  public void testAddParentAssociation() {
    Object parent = new NoAnnotations();
    Object child = new NoAnnotations();
    
    associate(child).withParent(parent);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }

  @Test
  public void testRemoveChildAssociation() {
    Object parent = new NoAnnotations();
    Object child = new NoAnnotations();
    
    associate(parent).withChild(child);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    dissociate(parent).fromChild(child);

    validateNoAssociation(manager, parent, child);
  }
  
  @Test
  public void testRemoveParentAssociation() {
    Object parent = new NoAnnotations();
    Object child = new NoAnnotations();
    
    associate(child).withParent(parent);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    dissociate(child).fromParent(parent);

    validateNoAssociation(manager, parent, child);
  }
}

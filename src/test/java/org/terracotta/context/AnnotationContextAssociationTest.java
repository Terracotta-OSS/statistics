/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.context;

import org.junit.Test;
import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.context.annotations.ContextChild;
import org.terracotta.context.annotations.ContextParent;

import static org.terracotta.context.ContextAssociationTestUtil.*;

/**
 *
 * @author cdennis
 */
public class AnnotationContextAssociationTest {
  
  @Test
  public void testPublicChildAssociation() {
    
    PublicAnnotations parent = new PublicAnnotations();
    NoAnnotations child = new NoAnnotations();
    
    parent.child = child;
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }
  
  @Test
  public void testPublicParentAssociation() {
    
    PublicAnnotations child = new PublicAnnotations();
    NoAnnotations parent = new NoAnnotations();
    
    child.parent = parent;
    
    ContextManager manager = new ContextManager();
    
    manager.root(child);
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }
  
  @Test
  public void testPrivateChildAssociation() {
    
    PrivateAnnotations parent = new PrivateAnnotations();
    NoAnnotations child = new NoAnnotations();
    
    parent.setChild(child);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }
  
  @Test
  public void testPrivateParentAssociation() {
    
    PrivateAnnotations child = new PrivateAnnotations();
    NoAnnotations parent = new NoAnnotations();
    
    child.setParent(parent);
    
    ContextManager manager = new ContextManager();
    
    manager.root(child);
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }
  
  @Test
  public void testPublicBiDirectionalAssociation() {
    
    PublicAnnotations parent = new PublicAnnotations();
    PublicAnnotations child = new PublicAnnotations();
    
    parent.child = child;
    child.parent = parent;
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }
  
  @Test
  public void testPrivateBiDirectionalAssociation() {
    
    PrivateAnnotations parent = new PrivateAnnotations();
    PrivateAnnotations child = new PrivateAnnotations();
    
    parent.setChild(child);
    child.setParent(parent);
    
    ContextManager manager = new ContextManager();
    
    manager.root(parent);
    
    validateAssociation(manager, parent, child);
  }
}

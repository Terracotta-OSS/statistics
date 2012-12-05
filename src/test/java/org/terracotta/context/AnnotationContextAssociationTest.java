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

import org.junit.Test;
import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.context.annotations.ContextChild;
import org.terracotta.context.annotations.ContextParent;

import static org.terracotta.context.ContextTestUtils.*;

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

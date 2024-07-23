/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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

import static org.terracotta.context.ContextTestUtils.*;
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

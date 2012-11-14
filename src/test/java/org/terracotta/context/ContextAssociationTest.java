/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.context.annotations.ContextChild;

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.ContextManager.*;
import static org.terracotta.context.query.QueryBuilder.*;

public class ContextAssociationTest {
  
  @Test
  public void testManualAssociation() {
    ContextManager contextManager = new ContextManager();
    
    Manager managerA = new Manager("managerA");
    Manager managerB = new Manager("managerB");
    Entity entity = new Entity();
    
    contextManager.root(managerA);
    associate(managerA).withChild(entity);
    associate(entity).withParent(managerB);
    associate(managerB).withParent(managerA);

    assertThat(contextManager.query(queryBuilder().build()), hasSize(1));
    
    TreeNode<Class, String, Object> child = contextManager.queryForSingleton(queryBuilder().children().build());
    
    assertThat(child.getContext().identifier(), IsEqual.<Object>equalTo(Manager.class));
    assertThat(child.getContext().attributes(), IsMapContaining.<Object, Object>hasEntry("name", "managerA"));
    assertThat(child.getChildren(), hasSize(2));
  }
  
  static class Manager {
    
    @ContextAttribute("name")
    private final String name;

    private Manager(String name) {
      this.name = name;
    }
  }
  
  static class Entity {

    @ContextAttribute("name")
    public String name() {
      return "orange";
    }
  }
  
  @Test
  public void testAnnotationAssociation() {
    ContextManager contextManager = new ContextManager();
    
    Parent parent = new Parent();
    
    contextManager.root(parent);
    
    assertThat(contextManager.query(queryBuilder().children().build()), hasSize(1));
  }
  
  static class Parent {
    
    @ContextChild
    private final Child child = new Child();
  }
  
  static class Child {
    
  }
}

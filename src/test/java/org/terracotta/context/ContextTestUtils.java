/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Collection;
import java.util.Collections;

import org.junit.Assert;
import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.context.annotations.ContextChild;
import org.terracotta.context.annotations.ContextParent;

import static org.hamcrest.core.IsCollectionContaining.*;
import static org.hamcrest.core.IsSame.*;
import static org.terracotta.context.query.Matchers.*;
import static org.terracotta.context.query.QueryBuilder.*;

/**
 *
 * @author cdennis
 */
public final class ContextTestUtils {
  
  private ContextTestUtils() {
    //static
  }
  
  public static void validateAssociation(ContextManager manager, Object parent, Object child) {
    TreeNode<?, ?, ?> parentNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", parent)))).build());
    TreeNode<?, ?, ?> childNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", child)))).build());
    Assert.assertThat((Collection<TreeNode<?, ?, ?>>) parentNode.getChildren(), hasItem(sameInstance(childNode)));
  }
  
  public static void validateNoAssociation(ContextManager manager, Object parent, Object child) {
    TreeNode<?, ?, ?> parentNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", parent)))).build());
    Assert.assertTrue(queryBuilder().children().filter(context(attributes(hasAttribute("this", child)))).build().execute(Collections.singleton(parentNode)).isEmpty());
  }

  @ContextAttribute("this") public static class PublicAnnotations {
    @ContextChild
    public Object child;
    
    @ContextParent
    public Object parent;
  }
  
  @ContextAttribute("this") public static class PrivateAnnotations {
    @ContextChild
    private Object child;
    
    @ContextParent
    private Object parent;
    
    public void setChild(Object child) {
      this.child = child;
    }

    public void setParent(Object parent) {
      this.parent = parent;
    }
  }

  @ContextAttribute("this") public static class NoAnnotations {}  
}

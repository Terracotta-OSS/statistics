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

import java.util.Collection;
import java.util.Collections;

import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.terracotta.context.annotations.ContextAttribute;
import org.terracotta.context.annotations.ContextChild;
import org.terracotta.context.annotations.ContextParent;

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
    TreeNode parentNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", parent)))).build());
    TreeNode childNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", child)))).build());
    Assert.assertThat((Collection<TreeNode>) parentNode.getChildren(), IsCollectionContaining.<TreeNode>hasItem(sameInstance(childNode)));
  }
  
  public static void validateNoAssociation(ContextManager manager, Object parent, Object child) {
    TreeNode parentNode = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("this", parent)))).build());
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

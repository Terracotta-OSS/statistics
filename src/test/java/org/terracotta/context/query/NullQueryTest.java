/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.context.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.terracotta.context.TreeNode;

import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.query.QueryBuilder.*;
import static org.terracotta.context.query.QueryTestUtils.*;

@RunWith(Parameterized.class)
public class NullQueryTest {
  
  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][] {{NullQuery.INSTANCE}, {queryBuilder().build()}});
  }
  
  private final Query query;
  
  public NullQueryTest(Query query) {
    this.query = query;
  }

  
  @Test
  public void testEmptySetUnmodified() {
    assertThat(query.execute(Collections.<TreeNode<Object, Object, Object>>emptySet()), IsEmptyCollection.<TreeNode<Object, Object, Object>>empty());
  }
  
  @Test
  public void testPopulatedSetUnmodified() {
    Set<TreeNode<String, Object, Object>> nodes = new HashSet<TreeNode<String, Object, Object>>();
    nodes.add(createTreeNode("foo", Collections.emptyMap()));
    nodes.add(createTreeNode("bar", Collections.emptyMap()));
    
    Set<TreeNode<String, Object, Object>> result = query.execute(nodes);
    
    assertThat(result, equalTo(nodes));
  }
}

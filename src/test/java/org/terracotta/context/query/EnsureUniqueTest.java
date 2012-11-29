/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.terracotta.context.TreeNode;

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.core.IsCollectionContaining.*;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.query.QueryBuilder.*;
import static org.terracotta.context.query.QueryTestUtils.*;

@RunWith(Parameterized.class)
public class EnsureUniqueTest {
  
  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][] {{EnsureUnique.INSTANCE}, {queryBuilder().ensureUnique().build()}});
  }
  
  private final Query query;
  
  public EnsureUniqueTest(Query query) {
    this.query = query;
  }

  @Test
  public void testUniqueInput() {
    TreeNode<String, Object, Object> node = createTreeNode("foo");
    Set<TreeNode<String, Object, Object>> results = query.execute(Collections.singleton(node));
    assertThat(results, hasSize(1));
    assertThat(results, hasItem(node));
  }
  
  @Test(expected = IllegalStateException.class)
  public void testNonUniqueInput() {
    Set<TreeNode<String, Object, Object>> nodes = new HashSet<TreeNode<String, Object, Object>>();
    nodes.add(createTreeNode("foo"));
    nodes.add(createTreeNode("bar"));
    query.execute(nodes);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testEmptyInput() {
    query.execute(Collections.<TreeNode<Object, Object, Object>>emptySet());
  }
}

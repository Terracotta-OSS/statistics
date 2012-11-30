/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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
    assertThat(query.execute(Collections.<TreeNode>emptySet()), IsEmptyCollection.<TreeNode>empty());
  }
  
  @Test
  public void testPopulatedSetUnmodified() {
    Set<TreeNode> nodes = new HashSet<TreeNode>();
    nodes.add(createTreeNode(A.class));
    nodes.add(createTreeNode(B.class));
    
    Set<TreeNode> result = query.execute(nodes);
    
    assertThat(result, equalTo(nodes));
  }
}

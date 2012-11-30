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

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.query.QueryBuilder.*;
import static org.terracotta.context.query.QueryTestUtils.*;

/**
 *
 * @author cdennis
 */
@RunWith(Parameterized.class)
public class ChildrenTest {
  
  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][] {{Children.INSTANCE}, {queryBuilder().children().build()}});
  }
  
  private final Query query;
  
  public ChildrenTest(Query query) {
    this.query = query;
  }
  
  @Test
  public void testSingleNodeWithNoChildren() {
    assertThat(query.execute(Collections.singleton(createTreeNode(A.class))), IsEmptyCollection.<TreeNode>empty());
  }

  @Test
  public void testMultipleNodesWithNoChildren() {
    Set<TreeNode> nodes = new HashSet<TreeNode>();
    nodes.add(createTreeNode(A.class));
    nodes.add(createTreeNode(B.class));
    assertThat(query.execute(nodes), IsEmptyCollection.<TreeNode>empty());
  }

  @Test
  public void testSingleNodeWithChildren() {
    Set<TreeNode> children = new HashSet<TreeNode>();
    children.add(createTreeNode(A.class));
    children.add(createTreeNode(B.class));
    
    TreeNode node = createTreeNode(C.class, children);
    
    assertThat(query.execute(Collections.singleton(node)), equalTo(children));
  }
  
  @Test
  public void testMultipleNodesWithChildren() {
    Set<TreeNode> cChildren = new HashSet<TreeNode>();
    cChildren.add(createTreeNode(A.class));
    cChildren.add(createTreeNode(B.class));
    
    TreeNode c = createTreeNode(C.class, cChildren);
    
    Set<TreeNode> fChildren = new HashSet<TreeNode>();
    fChildren.add(createTreeNode(D.class));
    fChildren.add(createTreeNode(E.class));
    
    TreeNode f = createTreeNode(F.class, fChildren);
    
    Set<TreeNode> input = new HashSet<TreeNode>();
    input.add(c);
    input.add(f);
    
    Set<TreeNode> children = new HashSet<TreeNode>();
    children.addAll(fChildren);
    children.addAll(cChildren);
    
    assertThat(query.execute(input), equalTo(children));
  }

  @Test
  public void testMultipleNodesWithCommonChildren() {
    TreeNode a = createTreeNode(A.class);
    
    Set<TreeNode> bazChildren = new HashSet<TreeNode>();
    bazChildren.add(createTreeNode(B.class));
    bazChildren.add(a);
    
    TreeNode c = createTreeNode(C.class, bazChildren);
    
    Set<TreeNode> eChildren = new HashSet<TreeNode>();
    eChildren.add(createTreeNode(D.class));
    eChildren.add(a);
    
    TreeNode e = createTreeNode(E.class, eChildren);
    
    Set<TreeNode> input = new HashSet<TreeNode>();
    input.add(c);
    input.add(e);
    
    Set<TreeNode> children = new HashSet<TreeNode>();
    children.addAll(eChildren);
    children.addAll(bazChildren);
    assertThat(children, hasSize(3));
    
    assertThat(query.execute(input), equalTo(children));
    
  }

  @Test
  public void testNodeWIthChildrenAndExtraDescendants() {
    Set<TreeNode> cChildren = new HashSet<TreeNode>();
    cChildren.add(createTreeNode(A.class));
    cChildren.add(createTreeNode(B.class));
    
    TreeNode c = createTreeNode(C.class, cChildren);
    
    Set<TreeNode> fChildren = new HashSet<TreeNode>();
    fChildren.add(createTreeNode(D.class));
    fChildren.add(createTreeNode(E.class));
    fChildren.add(c);
    
    TreeNode f = createTreeNode(F.class, fChildren);
        
    assertThat(query.execute(Collections.singleton(f)), equalTo(fChildren));
    
  }
}

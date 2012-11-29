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

@RunWith(Parameterized.class)
public class DescendantsTest {
  
  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][] {{Descendants.INSTANCE}, {queryBuilder().descendants().build()}});
  }
  
  private final Query query;
  
  public DescendantsTest(Query query) {
    this.query = query;
  }
  
  @Test
  public void testSingleNodeWithNoDescendants() {
    assertThat(query.execute(Collections.singleton(createTreeNode("foo"))), IsEmptyCollection.<TreeNode<String, Object, Object>>empty());
  }

  @Test
  public void testMultipleNodesWithNoDescendants() {
    Set<TreeNode<String, Object, Object>> nodes = new HashSet<TreeNode<String, Object, Object>>();
    nodes.add(createTreeNode("foo"));
    nodes.add(createTreeNode("bar"));
    assertThat(query.execute(nodes), IsEmptyCollection.<TreeNode<String, Object, Object>>empty());
  }

  @Test
  public void testSingleNodeWithChildren() {
    Set<TreeNode<String, Object, Object>> children = new HashSet<TreeNode<String, Object, Object>>();
    children.add(createTreeNode("foo"));
    children.add(createTreeNode("bar"));
    
    TreeNode<String, Object, Object> node = createTreeNode("baz", children);
    
    assertThat(query.execute(Collections.singleton(node)), equalTo(children));
  }  

  @Test
  public void testSingleNodeWithChildrenAndGrandChildren() {
    Set<TreeNode<String, Object, Object>> grandChildren = new HashSet<TreeNode<String, Object, Object>>();
    grandChildren.add(createTreeNode("foo"));
    grandChildren.add(createTreeNode("bar"));

    Set<TreeNode<String, Object, Object>> children = new HashSet<TreeNode<String, Object, Object>>();
    children.add(createTreeNode("baz", grandChildren));
    children.add(createTreeNode("bat"));
    
    TreeNode<String, Object, Object> node = createTreeNode("fum", children);
    
    Set<TreeNode<String, Object, Object>> descendants = new HashSet<TreeNode<String, Object, Object>>();
    descendants.addAll(grandChildren);
    descendants.addAll(children);
    
    assertThat(query.execute(Collections.singleton(node)), equalTo(descendants));
  }
  
  @Test
  public void testMultipleNodesWithChildren() {
    Set<TreeNode<String, Object, Object>> bazChildren = new HashSet<TreeNode<String, Object, Object>>();
    bazChildren.add(createTreeNode("foo"));
    bazChildren.add(createTreeNode("bar"));
    
    TreeNode<String, Object, Object> baz = createTreeNode("baz", bazChildren);
    
    Set<TreeNode<String, Object, Object>> eveChildren = new HashSet<TreeNode<String, Object, Object>>();
    eveChildren.add(createTreeNode("alice"));
    eveChildren.add(createTreeNode("bob"));
    
    TreeNode<String, Object, Object> eve = createTreeNode("eve", eveChildren);
    
    Set<TreeNode<String, Object, Object>> input = new HashSet<TreeNode<String, Object, Object>>();
    input.add(baz);
    input.add(eve);
    
    Set<TreeNode<String, Object, Object>> children = new HashSet<TreeNode<String, Object, Object>>();
    children.addAll(eveChildren);
    children.addAll(bazChildren);
    
    assertThat(query.execute(input), equalTo(children));
  }

  @Test
  public void testMultipleNodesWithChildrenAndGrandchildren() {
    Set<TreeNode<String, Object, Object>> bazGrandchildren = new HashSet<TreeNode<String, Object, Object>>();
    bazGrandchildren.add(createTreeNode("frank"));
    bazGrandchildren.add(createTreeNode("joe"));
    
    Set<TreeNode<String, Object, Object>> bazChildren = new HashSet<TreeNode<String, Object, Object>>();
    bazChildren.add(createTreeNode("foo", bazGrandchildren));
    bazChildren.add(createTreeNode("bar"));
    
    TreeNode<String, Object, Object> baz = createTreeNode("baz", bazChildren);
    
    Set<TreeNode<String, Object, Object>> eveGrandchildren = new HashSet<TreeNode<String, Object, Object>>();
    eveGrandchildren.add(createTreeNode("brian"));
    eveGrandchildren.add(createTreeNode("stewie"));
    
    Set<TreeNode<String, Object, Object>> eveChildren = new HashSet<TreeNode<String, Object, Object>>();
    eveChildren.add(createTreeNode("alice", eveGrandchildren));
    eveChildren.add(createTreeNode("bob"));
    
    TreeNode<String, Object, Object> eve = createTreeNode("eve", eveChildren);
    
    Set<TreeNode<String, Object, Object>> input = new HashSet<TreeNode<String, Object, Object>>();
    input.add(baz);
    input.add(eve);
    
    Set<TreeNode<String, Object, Object>> descendants = new HashSet<TreeNode<String, Object, Object>>();
    descendants.addAll(eveChildren);
    descendants.addAll(eveGrandchildren);
    descendants.addAll(bazChildren);
    descendants.addAll(bazGrandchildren);
    
    assertThat(query.execute(input), equalTo(descendants));
  }
  
  @Test
  public void testMultipleNodesWithCommonChildren() {
    TreeNode<String, Object, Object> alice = createTreeNode("alice");
    
    Set<TreeNode<String, Object, Object>> bazChildren = new HashSet<TreeNode<String, Object, Object>>();
    bazChildren.add(createTreeNode("foo"));
    bazChildren.add(alice);
    
    TreeNode<String, Object, Object> baz = createTreeNode("baz", bazChildren);
    
    Set<TreeNode<String, Object, Object>> eveChildren = new HashSet<TreeNode<String, Object, Object>>();
    eveChildren.add(createTreeNode("bob"));
    eveChildren.add(alice);
    
    TreeNode<String, Object, Object> eve = createTreeNode("eve", eveChildren);
    
    Set<TreeNode<String, Object, Object>> input = new HashSet<TreeNode<String, Object, Object>>();
    input.add(baz);
    input.add(eve);
    
    Set<TreeNode<String, Object, Object>> children = new HashSet<TreeNode<String, Object, Object>>();
    children.addAll(eveChildren);
    children.addAll(bazChildren);
    assertThat(children, hasSize(3));
    
    assertThat(query.execute(input), equalTo(children));
  }

  @Test
  public void testMultipleNodesWithCommonGrandchildren() {
    Set<TreeNode<String, Object, Object>> bazGrandchildren = new HashSet<TreeNode<String, Object, Object>>();
    bazGrandchildren.add(createTreeNode("frank"));
    bazGrandchildren.add(createTreeNode("joe"));
    
    Set<TreeNode<String, Object, Object>> bazChildren = new HashSet<TreeNode<String, Object, Object>>();
    bazChildren.add(createTreeNode("foo", bazGrandchildren));
    bazChildren.add(createTreeNode("bar"));
    
    TreeNode<String, Object, Object> baz = createTreeNode("baz", bazChildren);
    
    Set<TreeNode<String, Object, Object>> eveGrandchildren = new HashSet<TreeNode<String, Object, Object>>();
    eveGrandchildren.add(createTreeNode("brian"));
    eveGrandchildren.add(createTreeNode("stewie"));
    
    Set<TreeNode<String, Object, Object>> eveChildren = new HashSet<TreeNode<String, Object, Object>>();
    eveChildren.add(createTreeNode("alice", eveGrandchildren));
    eveChildren.add(createTreeNode("bob"));
    
    TreeNode<String, Object, Object> eve = createTreeNode("eve", eveChildren);
    
    Set<TreeNode<String, Object, Object>> input = new HashSet<TreeNode<String, Object, Object>>();
    input.add(baz);
    input.add(eve);
    
    Set<TreeNode<String, Object, Object>> descendants = new HashSet<TreeNode<String, Object, Object>>();
    descendants.addAll(eveChildren);
    descendants.addAll(eveGrandchildren);
    descendants.addAll(bazChildren);
    descendants.addAll(bazGrandchildren);
    
    assertThat(query.execute(input), equalTo(descendants));
  }
}

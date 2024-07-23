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
package org.terracotta.context.query;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.terracotta.context.TreeNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;
import static org.terracotta.context.query.QueryTestUtils.A;
import static org.terracotta.context.query.QueryTestUtils.B;
import static org.terracotta.context.query.QueryTestUtils.C;
import static org.terracotta.context.query.QueryTestUtils.D;
import static org.terracotta.context.query.QueryTestUtils.E;
import static org.terracotta.context.query.QueryTestUtils.F;
import static org.terracotta.context.query.QueryTestUtils.createTreeNode;

/**
 * @author cdennis
 */
@RunWith(Parameterized.class)
public class ChildrenTest {

  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][]{{Children.INSTANCE}, {queryBuilder().children().build()}});
  }

  private final Query query;

  public ChildrenTest(Query query) {
    this.query = query;
  }

  @Test
  public void testSingleNodeWithNoChildren() {
    assertThat(query.execute(Collections.singleton(createTreeNode(A.class))), IsEmptyCollection.empty());
  }

  @Test
  public void testMultipleNodesWithNoChildren() {
    Set<TreeNode> nodes = new HashSet<>();
    nodes.add(createTreeNode(A.class));
    nodes.add(createTreeNode(B.class));
    assertThat(query.execute(nodes), IsEmptyCollection.empty());
  }

  @Test
  public void testSingleNodeWithChildren() {
    Set<TreeNode> children = new HashSet<>();
    children.add(createTreeNode(A.class));
    children.add(createTreeNode(B.class));

    TreeNode node = createTreeNode(C.class, children);

    assertThat(query.execute(Collections.singleton(node)), equalTo(children));
  }

  @Test
  public void testMultipleNodesWithChildren() {
    Set<TreeNode> cChildren = new HashSet<>();
    cChildren.add(createTreeNode(A.class));
    cChildren.add(createTreeNode(B.class));

    TreeNode c = createTreeNode(C.class, cChildren);

    Set<TreeNode> fChildren = new HashSet<>();
    fChildren.add(createTreeNode(D.class));
    fChildren.add(createTreeNode(E.class));

    TreeNode f = createTreeNode(F.class, fChildren);

    Set<TreeNode> input = new HashSet<>();
    input.add(c);
    input.add(f);

    Set<TreeNode> children = new HashSet<>();
    children.addAll(fChildren);
    children.addAll(cChildren);

    assertThat(query.execute(input), equalTo(children));
  }

  @Test
  public void testMultipleNodesWithCommonChildren() {
    TreeNode a = createTreeNode(A.class);

    Set<TreeNode> bazChildren = new HashSet<>();
    bazChildren.add(createTreeNode(B.class));
    bazChildren.add(a);

    TreeNode c = createTreeNode(C.class, bazChildren);

    Set<TreeNode> eChildren = new HashSet<>();
    eChildren.add(createTreeNode(D.class));
    eChildren.add(a);

    TreeNode e = createTreeNode(E.class, eChildren);

    Set<TreeNode> input = new HashSet<>();
    input.add(c);
    input.add(e);

    Set<TreeNode> children = new HashSet<>();
    children.addAll(eChildren);
    children.addAll(bazChildren);
    assertThat(children, hasSize(3));

    assertThat(query.execute(input), equalTo(children));

  }

  @Test
  public void testNodeWIthChildrenAndExtraDescendants() {
    Set<TreeNode> cChildren = new HashSet<>();
    cChildren.add(createTreeNode(A.class));
    cChildren.add(createTreeNode(B.class));

    TreeNode c = createTreeNode(C.class, cChildren);

    Set<TreeNode> fChildren = new HashSet<>();
    fChildren.add(createTreeNode(D.class));
    fChildren.add(createTreeNode(E.class));
    fChildren.add(c);

    TreeNode f = createTreeNode(F.class, fChildren);

    assertThat(query.execute(Collections.singleton(f)), equalTo(fChildren));

  }
}

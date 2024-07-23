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
import static org.terracotta.context.query.QueryTestUtils.G;
import static org.terracotta.context.query.QueryTestUtils.H;
import static org.terracotta.context.query.QueryTestUtils.I;
import static org.terracotta.context.query.QueryTestUtils.J;
import static org.terracotta.context.query.QueryTestUtils.createTreeNode;

@RunWith(Parameterized.class)
public class DescendantsTest {

  private final Query query;

  public DescendantsTest(Query query) {
    this.query = query;
  }

  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][]{{Descendants.INSTANCE}, {queryBuilder().descendants().build()}});
  }

  @Test
  public void testSingleNodeWithNoDescendants() {
    assertThat(query.execute(Collections.singleton(createTreeNode(A.class))), IsEmptyCollection.empty());
  }

  @Test
  public void testMultipleNodesWithNoDescendants() {
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
  public void testSingleNodeWithChildrenAndGrandChildren() {
    Set<TreeNode> grandChildren = new HashSet<>();
    grandChildren.add(createTreeNode(A.class));
    grandChildren.add(createTreeNode(B.class));

    Set<TreeNode> children = new HashSet<>();
    children.add(createTreeNode(C.class, grandChildren));
    children.add(createTreeNode(D.class));

    TreeNode node = createTreeNode(E.class, children);

    Set<TreeNode> descendants = new HashSet<>();
    descendants.addAll(grandChildren);
    descendants.addAll(children);

    assertThat(query.execute(Collections.singleton(node)), equalTo(descendants));
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
  public void testMultipleNodesWithChildrenAndGrandchildren() {
    Set<TreeNode> eGrandchildren = new HashSet<>();
    eGrandchildren.add(createTreeNode(A.class));
    eGrandchildren.add(createTreeNode(B.class));

    Set<TreeNode> eChildren = new HashSet<>();
    eChildren.add(createTreeNode(C.class, eGrandchildren));
    eChildren.add(createTreeNode(D.class));

    TreeNode e = createTreeNode(E.class, eChildren);

    Set<TreeNode> jGrandchildren = new HashSet<>();
    jGrandchildren.add(createTreeNode(F.class));
    jGrandchildren.add(createTreeNode(G.class));

    Set<TreeNode> jChildren = new HashSet<>();
    jChildren.add(createTreeNode(H.class, jGrandchildren));
    jChildren.add(createTreeNode(I.class));

    TreeNode j = createTreeNode(J.class, jChildren);

    Set<TreeNode> input = new HashSet<>();
    input.add(e);
    input.add(j);

    Set<TreeNode> descendants = new HashSet<>();
    descendants.addAll(jChildren);
    descendants.addAll(jGrandchildren);
    descendants.addAll(eChildren);
    descendants.addAll(eGrandchildren);

    assertThat(query.execute(input), equalTo(descendants));
  }

  @Test
  public void testMultipleNodesWithCommonChildren() {
    TreeNode a = createTreeNode(A.class);

    Set<TreeNode> cChildren = new HashSet<>();
    cChildren.add(createTreeNode(B.class));
    cChildren.add(a);

    TreeNode c = createTreeNode(C.class, cChildren);

    Set<TreeNode> eChildren = new HashSet<>();
    eChildren.add(createTreeNode(D.class));
    eChildren.add(a);

    TreeNode e = createTreeNode(E.class, eChildren);

    Set<TreeNode> input = new HashSet<>();
    input.add(c);
    input.add(e);

    Set<TreeNode> children = new HashSet<>();
    children.addAll(eChildren);
    children.addAll(cChildren);
    assertThat(children, hasSize(3));

    assertThat(query.execute(input), equalTo(children));
  }

  @Test
  public void testMultipleNodesWithCommonGrandchildren() {
    Set<TreeNode> eGrandchildren = new HashSet<>();
    eGrandchildren.add(createTreeNode(A.class));
    eGrandchildren.add(createTreeNode(B.class));

    Set<TreeNode> eChildren = new HashSet<>();
    eChildren.add(createTreeNode(C.class, eGrandchildren));
    eChildren.add(createTreeNode(D.class));

    TreeNode e = createTreeNode(E.class, eChildren);

    Set<TreeNode> jGrandchildren = new HashSet<>();
    jGrandchildren.add(createTreeNode(F.class));
    jGrandchildren.add(createTreeNode(G.class));

    Set<TreeNode> jChildren = new HashSet<>();
    jChildren.add(createTreeNode(H.class, jGrandchildren));
    jChildren.add(createTreeNode(I.class));

    TreeNode j = createTreeNode(J.class, jChildren);

    Set<TreeNode> input = new HashSet<>();
    input.add(e);
    input.add(j);

    Set<TreeNode> descendants = new HashSet<>();
    descendants.addAll(jChildren);
    descendants.addAll(jGrandchildren);
    descendants.addAll(eChildren);
    descendants.addAll(eGrandchildren);

    assertThat(query.execute(input), equalTo(descendants));
  }
}

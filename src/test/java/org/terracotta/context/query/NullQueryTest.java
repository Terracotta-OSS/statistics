/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;
import static org.terracotta.context.query.QueryTestUtils.A;
import static org.terracotta.context.query.QueryTestUtils.B;
import static org.terracotta.context.query.QueryTestUtils.createTreeNode;

@RunWith(Parameterized.class)
public class NullQueryTest {

  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][]{{NullQuery.INSTANCE}, {queryBuilder().build()}});
  }

  private final Query query;

  public NullQueryTest(Query query) {
    this.query = query;
  }

  @Test
  public void testEmptySetUnmodified() {
    assertThat(query.execute(Collections.emptySet()), IsEmptyCollection.empty());
  }

  @Test
  public void testPopulatedSetUnmodified() {
    Set<TreeNode> nodes = new HashSet<>();
    nodes.add(createTreeNode(A.class));
    nodes.add(createTreeNode(B.class));

    Set<TreeNode> result = query.execute(nodes);

    assertThat(result, equalTo(nodes));
  }
}

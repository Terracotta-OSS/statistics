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
package org.terracotta.context.extended;

import org.junit.Test;
import org.terracotta.context.ContextManager;
import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Matcher;
import org.terracotta.context.query.Query;
import org.terracotta.statistics.observer.OperationObserver;
import org.terracotta.statistics.strawman.GetResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.terracotta.context.extended.ExtendedMatchers.hasProperty;
import static org.terracotta.context.extended.ExtendedMatchers.hasTag;
import static org.terracotta.context.extended.ExtendedMatchers.hasTags;
import static org.terracotta.context.query.Matchers.attributes;
import static org.terracotta.context.query.Matchers.context;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;
import static org.terracotta.statistics.StatisticBuilder.operation;

public class ExtendedMatchersTest {

  private final OperationObserver<GetResult> observer = operation(GetResult.class)
      .named("get")
      .of(this)
      .tag("cache", "tier")
      .property("key", "value")
      .build();

  @Test
  public void testHasTag_found() throws Exception {
    Set<TreeNode> nodes = query(hasTag("cache"));
    assertThat(nodes, hasSize(1));
  }

  @Test
  public void testHasTag_notfound() throws Exception {
    Set<TreeNode> nodes = query(hasTag("xxx"));
    assertThat(nodes, empty());
  }

  @Test
  public void testHasTags() throws Exception {
    Set<TreeNode> nodes = query(hasTags("cache", "tier"));
    assertThat(nodes, hasSize(1));
  }

  @Test
  public void testHasTags_notfound() throws Exception {
    Set<TreeNode> nodes = query(hasTags("cache", "xxx"));
    assertThat(nodes, empty());
  }

  @Test
  public void testHasTagsColl() throws Exception {
    Set<TreeNode> nodes = query(hasTags(Arrays.asList("cache", "tier")));
    assertThat(nodes, hasSize(1));
  }

  @Test
  public void testHasTagsColl_notfound() throws Exception {
    Set<TreeNode> nodes = query(hasTags(Arrays.asList("cache", "xxx")));
    assertThat(nodes, empty());
  }

  @Test
  public void testHasPropertyKey_found() throws Exception {
    Set<TreeNode> nodes = query(hasProperty("key"));
    assertThat(nodes, hasSize(1));
  }

  @Test
  public void testHasPropertyKey_notfound() throws Exception {
    Set<TreeNode> nodes = query(hasProperty("xxx"));
    assertThat(nodes, empty());
  }

  @Test
  public void testHasPropertyKeyValue_found() throws Exception {
    Set<TreeNode> nodes = query(hasProperty("key", "value"));
    assertThat(nodes, hasSize(1));
  }

  @Test
  public void testHasPropertyKeyValue_keynotfound() throws Exception {
    Set<TreeNode> nodes = query(hasProperty("xxx", "value"));
    assertThat(nodes, empty());
  }

  @Test
  public void testHasPropertyKeyValue_valuenotequalfound() throws Exception {
    Set<TreeNode> nodes = query(hasProperty("key", "xxx"));
    assertThat(nodes, empty());
  }

  private Set<TreeNode> query(Matcher<Map<String, Object>> matcher) {
    Query statQuery = queryBuilder()
        .descendants()
        .filter(context(attributes(matcher)))
        .build();

    return statQuery.execute(Collections.singleton(ContextManager.nodeFor(this)));
  }
}

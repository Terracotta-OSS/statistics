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
package org.terracotta.statistics;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;
import org.terracotta.context.TreeNode;
import org.terracotta.context.query.Query;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.terracotta.context.query.Matchers.attributes;
import static org.terracotta.context.query.Matchers.context;
import static org.terracotta.context.query.Matchers.hasAttribute;
import static org.terracotta.context.query.QueryBuilder.queryBuilder;
import static org.terracotta.statistics.StatisticType.COUNTER;
import static org.terracotta.statistics.StatisticType.GAUGE;

/**
 *
 * @author cdennis
 */
public class PassThroughStatisticTest {

  @Test
  public void testClean() {
    StatisticsManager.createPassThroughStatistic(this, "mystat",
        Collections.emptySet(), COUNTER, () -> 12);

    assertTrue(PassThroughStatistic.hasStatisticsFor(this));

    StatisticsManager.nodeFor(this).clean();

    assertFalse(PassThroughStatistic.hasStatisticsFor(this));

    StatisticsManager manager = new StatisticsManager();
    manager.root(this);

    Query query = queryBuilder().descendants().filter(context(attributes(hasAttribute("name", "mystat")))).build();
    Set<TreeNode> nodes = manager.query(query);
    assertThat(nodes, IsEmptyCollection.empty());
  }

  @Test
  public void testAnnotationBasedStatDetection() {
    StatisticsManager manager = new StatisticsManager();
    manager.root(new Foo());

    TreeNode foo = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("name", "foostat")))).build());
    TreeNode bar = manager.queryForSingleton(queryBuilder().descendants().filter(context(attributes(hasAttribute("name", "barstat")))).build());

    ValueStatistic<Number> fooStat = extractThis(foo);
    ValueStatistic<Number> barStat = extractThis(bar);

    assertThat(fooStat.value(), equalTo(42));
    assertThat(fooStat.type(), equalTo(COUNTER));
    assertThat(barStat.value(), equalTo(42L));
    assertThat(barStat.type(), equalTo(GAUGE));
  }

  @SuppressWarnings("unchecked")
  private ValueStatistic<Number> extractThis(TreeNode foo) {
    return (ValueStatistic<Number>)foo.getContext().attributes().get("this");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAnnotationBasedStatFailsWithParameter() {
    new StatisticsManager().root(new Object() {
      @Statistic(name = "foo", type = COUNTER)
      public Integer foo(String haha) {
        return 42;
      }
    });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAnnotationBasedStatFailsIfStatic() {
    new StatisticsManager().root(new FooStatic());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testAnnotationBasedStatFailsWithIncorrectReturn() {
    new StatisticsManager().root(new Object() {
      @Statistic(name = "foo", type = COUNTER)
      public String foo() {
        return "42";
      }
    });
  }

  static class Foo {

    @Statistic(name = "foostat", type = COUNTER)
    public Integer foo() {
      return 42;
    }

    @Statistic(name = "barstat", type = GAUGE)
    public long bar() {
      return 42L;
    }
  }
  
  static class FooStatic {
    @Statistic(name = "foo", type = COUNTER)
    public static Integer foo() {
      return 42;
    }
  }

}

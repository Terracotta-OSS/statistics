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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.terracotta.context.TreeNode;

import static org.junit.Assert.assertThat;
import static org.terracotta.context.query.QueryBuilder.*;
import static org.terracotta.context.query.QueryTestUtils.*;

/**
 *
 * @author cdennis
 */
@RunWith(Parameterized.class)
public class FilterTest {
  
  @Parameterized.Parameters
  public static List<Object[]> queries() {
    return Arrays.asList(new Object[][] {{"constructor"}, {"builder"}});
  }
  
  private final String buildHow;
  
  public FilterTest(String buildHow) {
    this.buildHow = buildHow;
  }

  @Test(expected=NullPointerException.class)
  public void testNullFilterFailsOnConstruction() {
    buildQuery(null);
  }
  
  @Test(expected=UnsupportedOperationException.class)
  public void testMatcherExceptionPropagates() {
    buildQuery(new Matcher() {

      @Override
      protected boolean matchesSafely(Object object) {
        throw new UnsupportedOperationException();
      }
    }).execute(Collections.singleton(createTreeNode(A.class)));
  }
  
  @Test
  public void testAlwaysFailMatcher() {
    Set<TreeNode> input = new HashSet<>();
    input.add(createTreeNode(A.class));
    input.add(createTreeNode(B.class));
    
    assertThat(buildQuery(new Matcher() {

      @Override
      protected boolean matchesSafely(Object object) {
        return false;
      }
    }).execute(input), IsEmptyCollection.empty());
  }
  
  @Test
  public void testAlwaysPassMatcher() {
    Set<TreeNode> input = new HashSet<>();
    input.add(createTreeNode(A.class));
    input.add(createTreeNode(B.class));
    
    assertThat(buildQuery(new Matcher() {

      @Override
      protected boolean matchesSafely(Object object) {
        return true;
      }
    }).execute(input), IsEqual.equalTo(input));
  }
  
  @Test
  public void testHalfPassMatcher() {
    Set<TreeNode> input = new HashSet<>();
    input.add(createTreeNode(A.class));
    input.add(createTreeNode(B.class));
    
    assertThat(buildQuery(new Matcher() {

      private boolean match;
      
      @Override
      protected boolean matchesSafely(Object object) {
        return match ^= true;
      }
    }).execute(input), IsCollectionWithSize.hasSize(input.size() / 2));
  }  

  private Query buildQuery(Matcher matcher) {
    if ("constructor".equals(buildHow)) {
      return new Filter(matcher);
    } else if ("builder".equals(buildHow)) {
      return queryBuilder().filter(matcher).build();
    } else {
      throw new AssertionError();
    }
  }
  
}

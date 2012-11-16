/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
      public boolean matches(Object object) {
        throw new UnsupportedOperationException();
      }
    }).execute(Collections.singleton(createTreeNode("foo")));
  }
  
  @Test
  public void testAlwaysFailMatcher() {
    Set<TreeNode<String, Object, Object>> input = new HashSet<TreeNode<String, Object, Object>>();
    input.add(createTreeNode("foo"));
    input.add(createTreeNode("bar"));
    
    assertThat(buildQuery(new Matcher() {

      @Override
      public boolean matches(Object object) {
        return false;
      }
    }).execute(input), IsEmptyCollection.<TreeNode<String, Object, Object>>empty());
  }
  
  @Test
  public void testAlwaysPassMatcher() {
    Set<TreeNode<String, Object, Object>> input = new HashSet<TreeNode<String, Object, Object>>();
    input.add(createTreeNode("foo"));
    input.add(createTreeNode("bar"));
    
    assertThat(buildQuery(new Matcher() {

      @Override
      public boolean matches(Object object) {
        return true;
      }
    }).execute(input), IsEqual.equalTo(input));
  }
  
  @Test
  public void testHalfPassMatcher() {
    Set<TreeNode<String, Object, Object>> input = new HashSet<TreeNode<String, Object, Object>>();
    input.add(createTreeNode("foo"));
    input.add(createTreeNode("bar"));
    
    assertThat(buildQuery(new Matcher() {

      private boolean match;
      
      @Override
      public boolean matches(Object object) {
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

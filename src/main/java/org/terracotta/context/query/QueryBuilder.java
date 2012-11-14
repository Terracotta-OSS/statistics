/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.terracotta.context.ContextElement;
import org.terracotta.context.TreeNode;

public class QueryBuilder {

  private Query current;
  
  private QueryBuilder() {
    current = NullQuery.INSTANCE;
  }
  
  public static QueryBuilder queryBuilder() {
    return new QueryBuilder();
  }
  
  public QueryBuilder filter(Matcher<? super TreeNode<?, ?, ?>> filter) {
    return chain(new Filter(filter));
  }
  
  public QueryBuilder children() {
    return chain(Children.INSTANCE);
  }
  
  public QueryBuilder descendants() {
    return chain(Descendants.INSTANCE);
  }
  
  public QueryBuilder chain(Query query) {
    current = new ChainedQuery(current, query);
    return this;
  }
  
  public Query build() {
    return current;
  }

  public static <I, K, V> Matcher<? super TreeNode<?, ?, ?>> context(final Matcher<ContextElement<? extends I, ? extends K, ? extends V>> matcher) {
    return new TypeSafeMatcher<TreeNode<?, ?, ?>>() {

      @Override
      protected boolean matchesSafely(TreeNode<?, ?, ?> t) {
        return matcher.matches(t.getContext());
      }

      @Override
      public void describeTo(Description d) {
        d.appendText("a context that has ").appendDescriptionOf(matcher);
      }
    };
  }
  
  public static <K, V> Matcher<ContextElement<?, ?, ?>> attributes(final Matcher<Map<? extends K, ? extends V>> matcher) {
    return new TypeSafeMatcher<ContextElement<?, ?, ?>>() {

      @Override
      protected boolean matchesSafely(ContextElement<?, ?, ?> t) {
        return matcher.matches(t.attributes());
      }

      @Override
      public void describeTo(Description d) {
        d.appendText("an attributes ").appendDescriptionOf(matcher);
      }
    };
  }
  
  public static <I> Matcher<ContextElement<?, ?, ?>> identifier(final Matcher<? extends I> matcher) {
    return new TypeSafeMatcher<ContextElement<?, ?, ?>>() {

      @Override
      protected boolean matchesSafely(ContextElement<?, ?, ?> t) {
        return matcher.matches(t.identifier());
      }

      @Override
      public void describeTo(Description d) {
        d.appendText("an identifier that is ").appendDescriptionOf(matcher);
      }
    };
    
  }
  
  public static Matcher<Class<?>> subclassOf(final Class<?> klazz) {
    return new TypeSafeMatcher<Class<?>>() {

      @Override
      protected boolean matchesSafely(Class<?> t) {
        return klazz.isAssignableFrom(t);
      }

      @Override
      public void describeTo(Description d) {
        d.appendText("a subtype of ").appendValue(klazz);
      }
    };
  }
}

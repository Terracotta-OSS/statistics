/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.query;

public interface Matcher<T> {
  
  boolean matches(T object);
}

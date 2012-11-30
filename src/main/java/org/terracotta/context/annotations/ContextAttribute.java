/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field, method, or the object itself as a context attribute.
 * <p>
 * Annotated final fields may be fetched eagerly at context creation time.  Non
 * static fields and methods are fetched both lazily and on also on every
 * attribute access.  Annotating the class type will associate {@code this}
 * with the supplied attribute name.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface ContextAttribute {
  
  /**
   * The name with which this attribute should be associated.
   * 
   * @return the attribute name
   */
  String value();
}

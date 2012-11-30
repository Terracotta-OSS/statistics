/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Map;

/**
 * A shadow context associated with a Java object.
 */
public interface ContextElement {
  
  /**
   * The type of the associated Java object.
   * 
   * @return the associated object's class
   */
  Class identifier();

  /**
   * The set of attributes for the associated Java object.
   * 
   * @return the associated object's attributes
   */
  Map<String, Object> attributes();
}

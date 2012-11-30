/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.context;

import java.util.Map;

public interface ContextElement {
  
  Class identifier();
  
  Map<String, Object> attributes();
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.statistics.archive;

/**
 *
 * @author cdennis
 */
public interface Timestamped<T> {
  
  T getSample();
  
  long getTimestamp();
}

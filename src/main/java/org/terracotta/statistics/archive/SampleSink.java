/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.statistics.archive;

/**
 *
 * @author cdennis
 */
public interface SampleSink<T> {
  
  void accept(T object);
}

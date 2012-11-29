/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.statistics.archive;

/**
 *
 * @author cdennis
 */
public class DevNull implements SampleSink<Object> {

  public static final SampleSink<Object> DEV_NULL = new DevNull();
  
  private DevNull() {
    //singleton
  }
  
  @Override
  public void accept(Object object) {
    //no-op
  }
}

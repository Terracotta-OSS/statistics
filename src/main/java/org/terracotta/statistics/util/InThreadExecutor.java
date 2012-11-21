/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.statistics.util;

import java.util.concurrent.Executor;

/**
 *
 * @author cdennis
 */
public final class InThreadExecutor implements Executor {
  
  public static final Executor INSTANCE = new InThreadExecutor();
  
  private InThreadExecutor() {
    //singleton
  }

  @Override
  public void execute(Runnable r) {
    r.run();
  }
}

/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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

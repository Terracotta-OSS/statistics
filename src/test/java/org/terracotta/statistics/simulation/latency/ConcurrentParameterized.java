/*
 * All content copyright Terracotta, Inc., unless otherwise indicated.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.statistics.simulation.latency;

import org.junit.runners.Parameterized;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.RunnerScheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Enables to run {@link Parameterized} Junit's test in parallel
 *
 * @author Mathieu Carbou
 */
public class ConcurrentParameterized extends Parameterized {

  public ConcurrentParameterized(Class<?> klass) throws Throwable {
    super(klass);

    ConcurrencyLevel annotation = klass.getAnnotation(ConcurrencyLevel.class);
    float threads = Runtime.getRuntime().availableProcessors() * (annotation == null ? 1.0f : annotation.value());
    ExecutorService executorService = Executors.newFixedThreadPool((int) threads);

    setScheduler(new DefaultScheduler() {
      @Override
      public void finished() {
        try {
          executorService.shutdown();
          executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        }
      }
    });

    getChildren().stream()
        .filter(ParentRunner.class::isInstance)
        .map(ParentRunner.class::cast)
        .forEach(runner -> runner.setScheduler(new DefaultScheduler() {
          @Override
          public void schedule(Runnable childStatement) {
            executorService.submit(childStatement);
          }
        }));
  }

  /**
   * Sets the ratio of {@link Runtime#availableProcessors()} ()} you want ot use as the thread amount.
   * <p>
   * Default is 1 times the available processors;
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Target(ElementType.TYPE)
  public @interface ConcurrencyLevel {
    float value() default 1.0f;
  }


  private static class DefaultScheduler implements RunnerScheduler {
    @Override
    public void schedule(Runnable childStatement) {
      childStatement.run();
    }

    @Override
    public void finished() {
    }
  }

}

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

import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToLongFunction;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Mathieu Carbou
 */
public class StreamUtils {

  public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<? super T> predicate) {
    return StreamSupport.stream(takeWhile(stream.spliterator(), predicate), false);
  }

  public static <T> Spliterator<T> takeWhile(Spliterator<T> splitr, Predicate<? super T> predicate) {
    return new Spliterators.AbstractSpliterator<T>(splitr.estimateSize(), splitr.characteristics()) {
      boolean stillGoing = true;

      @Override
      public boolean tryAdvance(Consumer<? super T> consumer) {
        if (stillGoing) {
          boolean hadNext = splitr.tryAdvance(elem -> {
            if (predicate.test(elem)) {
              consumer.accept(elem);
            } else {
              stillGoing = false;
            }
          });
          return hadNext && stillGoing;
        }
        return false;
      }
    };
  }

  // https://www.javamex.com/tutorials/random_numbers/gaussian_distribution_2.shtml
  public static DoubleSupplier nextGaussian(Random random, double desiredMean, double desiredStandardDeviation) {
    return () -> {
      double v;
      do {
        v = random.nextGaussian() * desiredStandardDeviation + desiredMean;
      } while (v <= 0);
      return v;
    };
  }

  public static LongSupplier toLong(DoubleSupplier ds, DoubleToLongFunction fn) {
    return () -> fn.applyAsLong(ds.getAsDouble());
  }

}

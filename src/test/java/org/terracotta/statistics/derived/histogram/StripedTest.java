/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.statistics.derived.histogram;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.util.Collections.nCopies;
import static java.util.concurrent.Executors.callable;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StripedTest {


  @Test(expected = NullPointerException.class)
  public void nullSupplierCausesNPE() {
    new Striped<>(null);
  }

  @Test
  public void uncontestedStripedUsesBase() {
    Foo foo = mock(Foo.class);

    @SuppressWarnings("unchecked")
    Supplier<Foo> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn(foo).thenThrow(new AssertionError());

    Striped<Foo> striped = new Striped<>(supplier);

    striped.process(Foo::foo);

    verify(foo).foo();
  }

  @Test
  public void contestedStripedExpands() throws InterruptedException {
    CyclicBarrier barrier = new CyclicBarrier(2);

    Foo fooBase = mock(Foo.class);
    Foo fooStripe = mock(Foo.class);
    when(fooBase.foo()).then(invocation -> barrier.await());
    when(fooStripe.foo()).then(invocation -> barrier.await());

    @SuppressWarnings("unchecked")
    Supplier<Foo> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn(fooBase, fooStripe).thenThrow(new AssertionError());

    Striped<Foo> striped = new Striped<>(supplier);

    Thread t1 = runInThread(() -> striped.process(Foo::foo)); //uses base
    Thread t2 = runInThread(() -> striped.process(Foo::foo)); //uses foo1

    t1.join();
    t2.join();

    verify(supplier, times(2)).get();
    verify(fooBase).foo();
    verify(fooStripe).foo();
  }

  @Test
  public void allExecutionsOccur() throws InterruptedException {
    Striped<int[]> striped = new Striped<>(() -> new int[1]);

    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    try {
      executorService.invokeAll(nCopies(100, callable(() -> striped.process(a -> a[0]++))));
    } finally {
      executorService.shutdown();
    }

    assertThat(striped.stream().mapToInt(a -> a[0]).sum(), is(100));
  }

  interface Foo {

    Object foo();
  }

  Thread runInThread(Runnable r) throws InterruptedException {
    Thread t = new Thread(r);
    t.start();
    return t;
  }
}



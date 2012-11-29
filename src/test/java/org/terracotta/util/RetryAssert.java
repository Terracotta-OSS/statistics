/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.junit.Assert;

public class RetryAssert {

    protected RetryAssert() {
        // static only class
    }

    public static <T> void assertBy(long time, TimeUnit unit, Callable<T> value, Matcher<? super T> matcher) {
        boolean interrupted = false;
        long end = System.nanoTime() + unit.toNanos(time);
        try {
            for (long sleep = 10; ; sleep <<= 1L) {
                try {
                    Assert.assertThat(value.call(), matcher);
                    return;
                } catch (Throwable t) {
                    //ignore - wait for timeout
                }

                long remaining = end - System.nanoTime();
                if (remaining <= 0) {
                    break;
                } else {
                    try {
                        Thread.sleep(Math.min(sleep, TimeUnit.NANOSECONDS.toMillis(remaining) + 1));
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            Assert.assertThat(value.call(), matcher);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}

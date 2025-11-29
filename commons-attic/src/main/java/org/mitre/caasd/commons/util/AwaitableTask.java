package org.mitre.caasd.commons.util;

import java.util.concurrent.CountDownLatch;

/**
 * An AwaitableTask is a simple wrapper that adds a CountDownLatch.countDown() call to a Runnable.
 * The purpose of this decorator is to enable the easy execution of parallel tasks.
 */
public class AwaitableTask implements Runnable {

    public final Runnable runMe;

    public final CountDownLatch latch;

    /**
     * An AwaitableTask is a Decorator that decrements a CountDownLatch when it finishes running the
     * provided Runnable
     *
     * @param latch A latch that is triggered after the Runnable is complete
     * @param runMe Work to be completed before the latch is triggered
     */
    public AwaitableTask(CountDownLatch latch, Runnable runMe) {
        this.latch = latch;
        this.runMe = runMe;
    }

    /**
     * Run the runnable, then call countDown on the latch.
     */
    @Override
    public void run() {
        runMe.run();
        latch.countDown();
    }
}

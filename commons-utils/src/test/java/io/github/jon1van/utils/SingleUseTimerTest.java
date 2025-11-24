package io.github.jon1van.utils;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class SingleUseTimerTest {

    public SingleUseTimerTest() {}

    @Test
    public void testDidntCallTicException() {

        SingleUseTimer timer = new SingleUseTimer();
        assertThrows(IllegalStateException.class, () -> timer.toc());
    }

    @Test
    public void testDidntCallTicException2() {

        SingleUseTimer timer = new SingleUseTimer();
        assertThrows(IllegalStateException.class, () -> timer.elapsedTime());
    }

    @Test
    public void testCallTicTwiceException() {

        SingleUseTimer timer = new SingleUseTimer();
        timer.tic();

        // Should not be able to call tic twice
        assertThrows(IllegalStateException.class, () -> timer.tic());
    }

    @Test
    public void testCallTocTwiceException() {

        SingleUseTimer timer = new SingleUseTimer();
        timer.tic();
        timer.toc();
        // Should not be able to call toc twice
        assertThrows(IllegalStateException.class, () -> timer.toc());
    }

    @Test
    public void testTimeContinuesToIncreaseIfTocNotCalled() {

        SingleUseTimer timer = new SingleUseTimer();

        timer.tic();
        Duration time1 = timer.elapsedTime();
        wasteTime(100);
        Duration time2 = timer.elapsedTime();
        wasteTime(100);
        Duration time3 = timer.elapsedTime();

        assertThat(time1.toNanos() < time2.toNanos()).isTrue();
        assertThat(time2.toNanos() < time3.toNanos()).isTrue();
    }

    @Test
    public void testTimeStopsIncreasingIfTocCalled() {

        SingleUseTimer timer = new SingleUseTimer();

        timer.tic();
        timer.toc();
        Duration time1 = timer.elapsedTime();
        wasteTime(100);
        Duration time2 = timer.elapsedTime();
        wasteTime(100);
        Duration time3 = timer.elapsedTime();

        assertThat(time1.toNanos() == time2.toNanos()).isTrue();
        assertThat(time2.toNanos() == time3.toNanos()).isTrue();
    }

    @Test
    public void testTiming1() {

        long MIN_DURATION_IN_MILLISEC = 300;

        SingleUseTimer timer = new SingleUseTimer();
        timer.tic();
        wasteTime(MIN_DURATION_IN_MILLISEC);
        timer.toc();
        Duration timeSpan = timer.elapsedTime();

        assertThat(timeSpan.toNanos() > 0).isTrue(); // some time elapsed
        assertThat(timeSpan.toMillis() < 2 * MIN_DURATION_IN_MILLISEC).isTrue(); // but not much
    }

    private void wasteTime(long millisec) {

        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }
}

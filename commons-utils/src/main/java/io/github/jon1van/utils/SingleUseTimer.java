package io.github.jon1van.utils;

import static com.google.common.base.Preconditions.checkState;

import java.time.Duration;
import java.time.Instant;

/// A SingleUseTimer computes the time elapsed between two points in a program. To use a
/// SingleUseTimer call "tic()" to start the timer and call "toc()" to stop the timer. The phrase
/// "single use" indicates the timer cannot be restarted once it has stopped (i.e. once the "toc()"
/// method was called).
///
/// The "elapsedTime()" method, CAN be called multiple times. If "toc()" has not been invoked
/// each call to "elapsedTime()" will return a new Duration (measured from the Instant "tic()" was
/// called).  Once "toc()" is called "elapsedTime()" always return the Duration between tic()
/// and toc().
public class SingleUseTimer {

    Instant startTime = null;
    Instant endTime = null;

    /// Start the timer. This method can only be called once.
    public void tic() {
        checkState(startTime == null, "This timer was already started");
        startTime = Instant.now();
    }

    /// Stop the timer. This method can only be called once. If called, this method must be called
    /// after tic().
    public void toc() {
        checkState(startTime != null, "Must call tic() before toc()");
        checkState(endTime == null, "This timer was already stopped");
        this.endTime = Instant.now();
    }

    /// If toc() was previously called then compute and return the time elapsed between tic() and
    /// toc(). If toc() was not called compute and return the time elapsed between calling tic() and
    /// calling this method.
    ///
    /// @return The time elapsed between tic() and toc() OR the time elapsed between tic() and this
    ///     method call.
    public Duration elapsedTime() {
        checkState(startTime != null, "This timer was never started (must call \"tic\")");

        return (endTime == null) ? Duration.between(startTime, Instant.now()) : Duration.between(startTime, endTime);
    }

    /// Prints ("Time elapsed: " + timeJob(timeMe).getSeconds() + " seconds") to Sys.out
    ///
    /// @param timeMe The Runnable will be executed
    public static void printTimeElapsed(Runnable timeMe) {
        System.out.println("Time elapsed: " + timeJob(timeMe).getSeconds() + " seconds");
    }

    /// Measure the time it takes for a Runnable to execute.
    ///
    /// @param timeMe The Runnable will be executed
    /// @return The time it takes for the Runnable to executed.
    public static Duration timeJob(Runnable timeMe) {
        SingleUseTimer timer = new SingleUseTimer();
        timer.tic();
        timeMe.run();
        timer.toc();
        return timer.elapsedTime();
    }
}

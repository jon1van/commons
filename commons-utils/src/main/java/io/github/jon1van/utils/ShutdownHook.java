package io.github.jon1van.utils;

import static java.util.Objects.nonNull;

import java.util.concurrent.ExecutorService;

/// ShutdownHook is a convenience class that makes it easier to properly shut down a java program that
/// contains an ExecutorService.
///
/// A ShutdownHook prevents a program that contains an ExecutorService from hanging when the user
/// presses "Control + C". A program with an ExecutorService can hang because the ExecutorService
/// contains a non-daemon thread that keeps the JVM from fully shutting down when "Control + C"
/// signal is given. A ShutdownHook receives the "Control + C" signal from the Runtime and tells its
/// ExecutorService it needs to shut down.
///
/// Note: Shutdown hooks will be executed if the user pressed "Control + C" but not if the user
/// presses "Control + Z"
public class ShutdownHook extends Thread {

    final ExecutorService exec;

    final Runnable shutdownWork;

    public static void addShutdownHookFor(ExecutorService exec) {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(exec));
    }

    public ShutdownHook(ExecutorService exec, Runnable shutdownWork) {
        this.exec = exec;
        this.shutdownWork = shutdownWork;
    }

    public ShutdownHook(ExecutorService exec) {
        this(exec, null);
    }

    @Override
    public void run() {
        System.out.println("\nLaunching shutdown hook.");
        System.out.print("\nShutting down an ExecutorService.");

        if (nonNull(shutdownWork)) {
            shutdownWork.run();
        }
        exec.shutdownNow();
    }
}

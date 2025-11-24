package io.github.jon1van.func;

import java.util.function.Consumer;

/// A CountingConsumer decorates a Consumer with a counting mechanic.
public class CountingConsumer<T> implements Consumer<T> {

    private int numCallsToAccept = 0;

    private final Consumer<T> wrappedConsumer;

    public CountingConsumer(Consumer<T> consumer) {
        this.wrappedConsumer = consumer;
    }

    public static <T> CountingConsumer<T> from(Consumer<T> consumer) {
        return new CountingConsumer<>(consumer);
    }

    /// Increment the counter and call the Consumer's accept method
    @Override
    public void accept(T t) {
        numCallsToAccept++;
        wrappedConsumer.accept(t);
    }

    /// @return The number of calls to the accept method
    public int acceptCount() {
        return numCallsToAccept;
    }

    /// @return The Consumer that was supplied at construction time
    public Consumer<T> consumer() {
        return wrappedConsumer;
    }
}

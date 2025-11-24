package io.github.jon1van.func;

import java.util.*;
import java.util.function.Consumer;

/// A CompositeConsumer is sequence of Consumers that mimics "Consumer.andThen" framing
/// @param <T> The common type shared between all the component consumers.
public class CompositeConsumer<T> implements Consumer<T> {

    private final List<Consumer<? super T>> allConsumers;

    @SafeVarargs
    public CompositeConsumer(Consumer<? super T>... consumers) {
        this.allConsumers = new LinkedList<>();
        Collections.addAll(allConsumers, consumers);
    }

    @SafeVarargs
    public static <T> CompositeConsumer<T> combine(Consumer<? super T>... consumers) {
        return new CompositeConsumer<>(consumers);
    }

    @Override
    public void accept(T t) {
        for (Consumer<? super T> consumer : allConsumers) {
            consumer.accept(t);
        }
    }

    ///  @return The Consumers that were provided at construction time.
    public List<Consumer<? super T>> consumers() {
        return Collections.unmodifiableList(allConsumers);
    }
}

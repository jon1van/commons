package io.github.jon1van.func;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

/// A TranslatingConsumer converts a `Consumer<AFTER>` into a `Consumer<BEFORE>`.
///
/// This is achieved by decorating a `Consumer<AFTER>` with a preceding "translation
/// function" `Function<BEFORE, AFTER>` that intercepts inputs of type BEFORE and converts them
/// to instances of type AFTER that are then fed into the inner decorated consumer.
///
/// @param <BEFORE> The type before the translation step (i.e. the upstream type)
/// @param <AFTER> The type after the translation step (i.e. the downstream type)
public class TranslatingConsumer<BEFORE, AFTER> implements Consumer<BEFORE> {

    private final Function<BEFORE, AFTER> translator;

    private final Consumer<AFTER> downStream;

    /// Wrap a downStream consumer with a preceding "type translation step".  This allows the
    /// downStream `Consumer<AFTER>` to masquerade as a `Consumer<BEFORE>`
    ///
    /// @param translator Converts inputs from the "BEFORE" type to the "AFTER" type (and sends them
    ///                   to the downStream consumer)
    /// @param downStream Receive the "post translation" type
    public TranslatingConsumer(Function<BEFORE, AFTER> translator, Consumer<AFTER> downStream) {
        this.translator = requireNonNull(translator);
        this.downStream = requireNonNull(downStream);
    }

    public static <IN, OUT> TranslatingConsumer<IN, OUT> of(Function<IN, OUT> translator, Consumer<OUT> downStream) {
        return new TranslatingConsumer<>(translator, downStream);
    }

    @Override
    public void accept(BEFORE input) {
        AFTER item = translator.apply(input);
        downStream.accept(item);
    }

    /// @return The "translating function" provided at construction time.
    public Function<BEFORE, AFTER> translator() {
        return this.translator;
    }

    /// @return The "downstream consumer" provided at construction time.
    public Consumer<AFTER> consumer() {
        return this.downStream;
    }
}

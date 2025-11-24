package io.github.jon1van.func;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

/// A FilteredConsumer decorates a Consumer with a preemptive filtering Predicate. The inner Consumer
/// only receives input items that pass the filter's boolean test.
///
/// If desired, inputs that fail the filter's boolean test will be passed to a 2nd Consumer. In this
/// mode, a FilteredConsumer can be thought of as a "T-junction" that routes data according to the
/// output of the filter's boolean test.
///
/// @param <T> The type of data used within these functions
public class FilteredConsumer<T> implements Consumer<T> {

    private final Predicate<T> filter;

    private final Consumer<T> whenTrue;

    private final Consumer<T> whenFalse;

    /// Bundle a preemptive filter and a downstream consumer that only receives "passing" items.
    ///
    /// @param filter   A boolean test applied to each incoming item T
    /// @param whenTrue The consumer that receives items that pass the filtering test
    public FilteredConsumer(Predicate<T> filter, Consumer<T> whenTrue) {
        this(filter, whenTrue, x -> {}); // discard items that fail the filter, basically /dev/null.
    }

    /// Bundle a preemptive filter and two downstream consumers.  One consumer receives only the
    /// "passing" items, the other consumer receives only the "failing" items.
    ///
    /// @param filter    A boolean test applied to each incoming item T
    /// @param whenTrue  The consumer that receives items that pass the filtering test
    /// @param whenFalse The consumer that receives items that fail the filtering test
    public FilteredConsumer(Predicate<T> filter, Consumer<T> whenTrue, Consumer<T> whenFalse) {
        requireNonNull(filter);
        requireNonNull(whenTrue);
        requireNonNull(whenFalse);
        this.filter = filter;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    /// Accept an input item, apply the boolean test, and pass it to the corresponding consumer.
    ///
    /// @param item An input that will be tested and routed.
    @Override
    public void accept(T item) {

        if (filter.test(item)) {
            whenTrue.accept(item);
        } else {
            whenFalse.accept(item);
        }
    }

    /// @return The filtering Predicate provided at construction time.
    public Predicate<T> filter() {
        return this.filter;
    }

    /// @return The Consumer provided at construction for receiving "passing" inputs.
    public Consumer<T> whenTrue() {
        return this.whenTrue;
    }

    /// @return The Consumer provided at construction for receiving "failing" inputs.  This will
    ///     be a "NO OP Lambda" (e.g. `x ->{}`) when a 2nd Consumer is not supplied
    public Consumer<T> whenFalse() {
        return this.whenFalse;
    }
}

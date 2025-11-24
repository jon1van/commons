package io.github.jon1van.func;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Predicate;

/// A CountingPredicate decorates a Predicate with counters that track the number of time a
/// Predicate is called, returns true, and returns false.
public class CountingPredicate<T> implements Predicate<T> {

    private final Predicate<T> predicate;

    private long numCalls;

    private long numTrue;

    private long numFalse;

    public CountingPredicate(Predicate<T> pred) {
        checkNotNull(pred);
        this.predicate = pred;
    }

    public static <T> CountingPredicate<T> from(Predicate<T> predicate) {
        return new CountingPredicate<>(predicate);
    }

    @Override
    public boolean test(T t) {
        numCalls++;
        boolean result = predicate.test(t);

        if (result) {
            numTrue++;
        } else {
            numFalse++;
        }
        return result;
    }

    /// @return The Predicate supplied at construction.
    public Predicate<T> predicate() {
        return predicate;
    }

    public void resetCounts() {
        numCalls = 0;
        numTrue = 0;
        numFalse = 0;
    }

    /// @return The number of times the "test" method is called.
    public long count() {
        return numCalls;
    }

    /// @return The number of times the "test" method returned true.
    public long trueCount() {
        return numTrue;
    }

    /// @return The number of times the "test" method returned false.
    public long falseCount() {
        return numFalse;
    }
}

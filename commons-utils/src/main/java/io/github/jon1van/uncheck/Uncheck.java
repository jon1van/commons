package io.github.jon1van.uncheck;

import static io.github.jon1van.uncheck.DemotedException.demote;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/// These methods makes using Java Streams and Java functional interfaces more convenient.
///
/// When a method you want to use as a lambda (e.g., [Runnable],  [Supplier], [Predicate], [Function], or [Consumer])
/// throws a Checked Exception you CANNOT use that method directly as-is because any method that throws a Checked
/// Exception will not match a [FunctionalInterface] in [java.util.function]. Typically, you will need to wrap your
/// method inside a try/catch block if you want to call it in a Java Stream.
///
/// These methods solve this problem by adapting "Checked" version of the FunctionalInterfaces in [java.util.function]
/// to the standard form of those interfaces. For example, a [CheckedFunction] is adapted into [Function] using the
/// [Uncheck#func(CheckedFunction)] method.
///
/// These methods use [DemotedException] to "demote" checked exceptions into RuntimeExceptions. Demoting exception
/// shortens syntax and greatly simplifies [Stream] operators:
///
/// **Bottom-line:** Uncheck removes the annoyance of littering functional code with try/catch blocks.
/// ```java
/// // Without Uncheck this simply stream would require two different try/catch blocks
/// myList.stream()
///     .filter(Uncheck.pred(x -> checkedTest(x)))
///     .map(Uncheck.func(x -> checkedFn(x)))
///     .toList();
/// ```
public class Uncheck {

    /// Demote a [CheckedFunction] that throws an [Exception] into a plain
    /// [Function].  This method is for simplifying stream processing pipelines.
    ///
    /// @param func A function that throws a checked exception
    /// @param <S>  The input type
    /// @param <T>  The output type
    /// @return A plain Function that may emit DemotedExceptions
    /// @throws DemotedException when the original CheckedFunction would have thrown a checked
    ///                                                                            Exception
    public static <S, T> Function<S, T> func(CheckedFunction<S, T> func) {
        return x -> {
            try {
                return func.apply(x);
            } catch (RuntimeException e) {
                // pass runtime exceptions, they cannot be demoted
                throw e;
            } catch (Exception e) {
                throw demote(e);
            }
        };
    }

    /// Demote a [CheckedPredicate] that throws an [Exception] into a plain
    /// [Predicate].  This method is for simplifying stream processing pipelines.
    ///
    /// @param pred A predicate that throws a checked exception
    /// @param <T>  The generic type
    /// @return A plain Predicate that may emit DemotedExceptions
    /// @throws DemotedException when the original CheckedPredicate would have thrown a checked Exception
    public static <T> Predicate<T> pred(CheckedPredicate<T> pred) {
        return x -> {
            try {
                return pred.test(x);
            } catch (RuntimeException e) {
                // pass runtime exceptions, they cannot be demoted
                throw e;
            } catch (Exception e) {
                throw demote(e);
            }
        };
    }

    /// Demote a [CheckedConsumer] that throws an [Exception] into a plain
    /// [Consumer].  This method is for simplifying stream processing pipelines.
    ///
    /// @param consumer A consumer that throws a checked exception
    /// @param <T>      The generic type
    /// @return A plain Consumer that may emit DemotedExceptions
    /// @throws DemotedException when the original CheckedConsumer would have thrown a checked Exception
    public static <T> Consumer<T> consumer(CheckedConsumer<T> consumer) {
        return x -> {
            try {
                consumer.accept(x);
            } catch (RuntimeException e) {
                // pass runtime exceptions, they cannot be demoted
                throw e;
            } catch (Exception e) {
                throw demote(e);
            }
        };
    }

    /// Demote a [CheckedBiFunction] that throws an [Exception] into a plain
    /// [BiFunction].  This method is for simplifying stream processing pipelines.
    ///
    /// @param biFunc A BiFunction that throws a checked exception
    /// @return A plain BiFunction that may emit DemotedExceptions
    /// @throws DemotedException when the original CheckedPredicate would have thrown a checked Exception
    public static <T, U, R> BiFunction<T, U, R> biFunc(CheckedBiFunction<T, U, R> biFunc) {
        return (t, u) -> {
            try {
                return biFunc.apply(t, u);
            } catch (RuntimeException e) {
                // pass runtime exceptions, they cannot be demoted
                throw e;
            } catch (Exception e) {
                throw demote(e);
            }
        };
    }

    /// Demote a [CheckedBinaryOperator] that throws an [Exception] into a plain
    /// [BinaryOperator].  This method is for simplifying stream processing pipelines.
    ///
    /// @param binaryOperator A BinaryOperator that throws a checked exception
    /// @return A plain BinaryOperator that may emit DemotedExceptions
    /// @throws DemotedException when the original CheckedBinaryOperator would have thrown a checked Exception
    public static <T> BinaryOperator<T> biOp(CheckedBinaryOperator<T> binaryOperator) {
        return (t, u) -> {
            try {
                return binaryOperator.apply(t, u);
            } catch (RuntimeException e) {
                // pass runtime exceptions, they cannot be demoted
                throw e;
            } catch (Exception e) {
                throw demote(e);
            }
        };
    }

    /// Demote a [CheckedSupplier] that throws an [Exception] into a plain
    /// [Supplier].  This method is for simplifying stream processing pipelines.
    ///
    /// @param checkedSupplier A supplier that throws a checked exception
    /// @param <T>             The supplied type
    /// @return A plain Supplier that may emit DemotedExceptions
    /// @throws DemotedException when the original CheckedSupplier would have thrown a checked Exception
    public static <T> Supplier<T> supplier(CheckedSupplier<T> checkedSupplier) {
        return () -> {
            try {
                return checkedSupplier.get();
            } catch (RuntimeException e) {
                // pass runtime exceptions, they cannot be demoted
                throw e;
            } catch (Exception e) {
                throw demote(e);
            }
        };
    }

    /// Wraps this "Runnable" with a try/catch block that demotes all checked exceptions.
    ///
    /// @param checkedRunnable A Runnable that can throw a checked exception
    public static void run(CheckedRunnable checkedRunnable) {
        try {
            checkedRunnable.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw demote(e);
        }
    }

    /// Wraps a Callable with a try/catch block that demotes all checked exceptions.
    ///
    /// @param callable A regular [Callable] that throws a checked exception
    public static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw demote(e);
        }
    }
}

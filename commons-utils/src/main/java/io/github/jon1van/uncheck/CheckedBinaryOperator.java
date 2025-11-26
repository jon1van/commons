package io.github.jon1van.uncheck;

/// [BinaryOperator] is to [BiFunction] as [CheckedBinaryOperator] is to
/// [CheckedBiFunction]
@FunctionalInterface
public interface CheckedBinaryOperator<T> extends CheckedBiFunction<T, T, T> {}

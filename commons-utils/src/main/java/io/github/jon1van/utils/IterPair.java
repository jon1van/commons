package io.github.jon1van.utils;

/// IterPair is short for "Iteration Neighbor Pair".  The goal of this class is to package two
/// consecutive elements from an iteration.
public record IterPair<T>(T prior, T current) {}

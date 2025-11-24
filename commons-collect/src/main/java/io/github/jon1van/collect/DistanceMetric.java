package io.github.jon1van.collect;

/// The DistanceMetric should define a true Metric Space (in the strict algebraic sense) for KEY
/// objects. This means the following should be true:
///
/// 1. d(x,y) >= 0
/// 2. d(x,y) = d(y,x)
/// 3. d(x,z) <= d(x,y) + d(y,z)
/// 4. d(x , y ) = 0 if and only if x = y (optional rule)
@FunctionalInterface
public interface DistanceMetric<KEY> {

    /// @param item1 The first of two items
    /// @param item2 The second of two items
    ///
    /// @return The distance between the 2 objects in a Metric Space (this method must define a
    ///     proper Metric Space in the strict algebraic sense).
    double distanceBtw(KEY item1, KEY item2);
}

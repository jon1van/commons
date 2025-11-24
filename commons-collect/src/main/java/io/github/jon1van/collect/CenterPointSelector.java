package io.github.jon1van.collect;

import java.util.List;

/// A CenterPointSelector selects two keys from a List of keys. The selected key are used as the
/// "Center Points" for the multi-dimensional spheres use in the MetricTree and MetricSet classes.
///
/// @param <K> The Key class
public interface CenterPointSelector<K> {

    /// @param keys   A List of Keys that needs to be split
    /// @param metric The distance metric that measures distance between 2 keys
    ///
    /// @return Two keys that will be used as the centerPoints for a two new Spheres
    Pair<K> selectNewCenterPoints(List<K> keys, DistanceMetric<K> metric);
}

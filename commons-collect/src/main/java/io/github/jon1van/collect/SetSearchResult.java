package io.github.jon1van.collect;

import java.util.Objects;

import com.google.common.primitives.Doubles;

/// SetSearchResult relay the output of a K-nearest neighbor search or a range search.
///
/// @param <K> The key class
public class SetSearchResult<K> implements Comparable<SetSearchResult<K>> {

    final K key;

    final double distance;

    SetSearchResult(K key, double distance) {
        this.key = key;
        this.distance = distance;
    }

    public K key() {
        return this.key;
    }

    public double distance() {
        return this.distance;
    }

    /// Sort by distance. This is required for the PriorityQueue used to collect the Results always
    /// has the Result with the k-th largest distance on top. This means the threshold for improving
    /// the k-nearest neighbor result is readily accessible.
    @Override
    public int compareTo(SetSearchResult<K> other) {
        return Doubles.compare(other.distance, this.distance);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.key);
        hash = 29 * hash
                + (int) (Double.doubleToLongBits(this.distance) ^ (Double.doubleToLongBits(this.distance) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SetSearchResult<?> other = (SetSearchResult<?>) obj;
        if (Double.doubleToLongBits(this.distance) != Double.doubleToLongBits(other.distance)) {
            return false;
        }
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }
}

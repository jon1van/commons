package io.github.jon1van.collect;

import java.util.Objects;

import com.google.common.primitives.Doubles;

/// SearchResults relay the output of a K-nearest neighbor search or a range search.
///
/// @param <K> The Key in a Key + Value pair
/// @param <V> The Value in a Key + Value pair
public class SearchResult<K, V> implements Comparable<SearchResult<K, V>> {

    final K key;

    final V value;

    final double distance;

    SearchResult(K key, V value, double distance) {
        this.key = key;
        this.value = value;
        this.distance = distance;
    }

    public K key() {
        return this.key;
    }

    public V value() {
        return this.value;
    }

    public double distance() {
        return this.distance;
    }

    /// Sort by distance. This is required for the PriorityQueue used to collect the Results always
    /// has the Result with the k-th largest distance on top. This means the threshold for improving
    /// the k-nearest neighbor result is readily accessible.
    @Override
    public int compareTo(SearchResult<K, V> other) {
        return Doubles.compare(other.distance, this.distance);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.key);
        hash = 79 * hash + Objects.hashCode(this.value);
        hash = 79 * hash
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
        final SearchResult<?, ?> other = (SearchResult<?, ?>) obj;
        if (Double.doubleToLongBits(this.distance) != Double.doubleToLongBits(other.distance)) {
            return false;
        }
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
}

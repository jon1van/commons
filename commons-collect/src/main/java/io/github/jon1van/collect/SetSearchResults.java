package io.github.jon1van.collect;

import static java.util.Collections.reverseOrder;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * SetSearchResults contain the results from a K-nearest Neighbors Search or a Range Search.  The
 * results are always sorted by distance to search Key.
 *
 * @param <K> The Key's making up the "Metric Space", we can measure the distance btw two Keys
 */
public class SetSearchResults<K> {

    /** The key provided at search time. */
    private final K searchKey;

    /** All result found during the Search operation. */
    private final ArrayList<SetSearchResult<K>> results;

    SetSearchResults(K searchKey, Collection<SetSearchResult<K>> c) {
        requireNonNull(searchKey);
        this.searchKey = searchKey;
        this.results = new ArrayList<>(c);
        results.sort(reverseOrder());
    }

    /** @return The Key upon which the search was based. */
    public K searchKey() {
        return searchKey;
    }

    /** @return True, when there is no data to report. */
    public boolean isEmpty() {
        return results.isEmpty();
    }

    /** @return The number of elements in the results set. */
    public int size() {
        return results.size();
    }

    /** Get all the result in a list sorted by distance (element 0 = "nearest neighbor"). */
    public List<SetSearchResult<K>> results() {
        return results;
    }

    /** Equivalent to {@code this.results().stream()}. */
    public Stream<SetSearchResult<K>> stream() {
        return results.stream();
    }

    /** Cherry-pick a single result from the sorted list of results (0 = closest Tuple). */
    public SetSearchResult<K> result(int i) {
        return results.get(i);
    }

    /** Get just the Keys from the search results, list sorted by distance. */
    public List<K> keys() {
        return results.stream().map(sr -> sr.key()).toList();
    }

    /** Get just the distances from the search results, list sorted by distance. */
    public List<Double> distances() {
        return results.stream().map(sr -> sr.distance()).toList();
    }
}

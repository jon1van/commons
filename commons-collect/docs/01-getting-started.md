# Commons Collect - Getting Started

Custom data structures for efficient k-nearest neighbor searches and ordered collections.

## Overview

Commons Collect provides:

- **MetricTree** - Map with k-nearest neighbor and range search
- **MetricSet** - Set with k-nearest neighbor and range search
- **HashedLinkedSequence** - Hybrid of HashSet and LinkedList

## Key Concepts

### Metric Space

A metric space defines distance between objects satisfying:

1. `d(x,y) >= 0` (non-negativity)
2. `d(x,y) = d(y,x)` (symmetry)
3. `d(x,z) <= d(x,y) + d(y,z)` (triangle inequality)

### DistanceMetric Interface

```java
@FunctionalInterface
public interface DistanceMetric<K> {
    double distanceBtw(K item1, K item2);
}
```

## Quick Examples

### MetricTree - Spatial Key-Value Store

```java
// Define how to measure distance between keys
DistanceMetric<LatLong> metric = (a, b) -> a.distanceTo(b).inMeters();

// Create the tree
MetricTree<LatLong, String> tree = new MetricTree<>(metric);

// Add data
tree.put(LatLong.of(40.7128, -74.0060), "New York");
tree.put(LatLong.of(34.0522, -118.2437), "Los Angeles");
tree.put(LatLong.of(41.8781, -87.6298), "Chicago");

// Find 3 closest cities to a point
LatLong searchPoint = LatLong.of(39.0, -90.0);
SearchResults<LatLong, String> results = tree.getNClosest(searchPoint, 3);

for (SearchResult<LatLong, String> r : results.results()) {
    System.out.println(r.value() + ": " + r.distance() + " meters");
}
```

### MetricSet - Spatial Set

```java
// Edit distance metric for strings
DistanceMetric<String> editDistance = (a, b) -> {
    // Levenshtein distance implementation
    return computeEditDistance(a, b);
};

MetricSet<String> dictionary = new MetricSet<>(editDistance);
dictionary.add("hello");
dictionary.add("hallo");
dictionary.add("help");

// Find similar words
SetSearchResults<String> similar = dictionary.getNClosest("helo", 3);
```

### HashedLinkedSequence - Ordered Set with Fast Lookup

```java
HashedLinkedSequence<String> sequence = HashedLinkedSequence.newHashedLinkedSequence();

sequence.addLast("a");
sequence.addLast("b");
sequence.addLast("c");

// O(1) lookup
boolean hasB = sequence.contains("b");  // true

// O(1) navigation
String afterA = sequence.getElementAfter("a");   // "b"
String beforeC = sequence.getElementBefore("c"); // "b"

// O(1) insertion at reference
sequence.insertAfter("x", "a");  // a, x, b, c
```

## When to Use What

| Data Structure | Use Case |
|----------------|----------|
| MetricTree | Key-value store needing spatial queries |
| MetricSet | Set membership with similarity search |
| HashedLinkedSequence | Ordered set with positional operations |

## Next Steps

- [MetricTree & MetricSet](02-metric-structures.md) - Spatial search structures
- [HashedLinkedSequence](03-hashed-linked-sequence.md) - Hybrid ordered set

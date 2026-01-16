# MetricTree & MetricSet

Efficient data structures for k-nearest neighbor and range searches in metric spaces.

## MetricTree

A map-like structure optimized for spatial queries. Stores key-value pairs and supports finding entries closest to a search key.

### Creating a MetricTree

```java
// Define distance metric
DistanceMetric<Point> metric = (p1, p2) ->
    Math.hypot(p1.x() - p2.x(), p1.y() - p2.y());

// Create tree
MetricTree<Point, String> tree = new MetricTree<>(metric);
```

### Adding and Removing Entries

```java
// Add entries
tree.put(new Point(0, 0), "origin");
tree.put(new Point(1, 1), "diagonal");
tree.put(new Point(5, 0), "right");

// Remove by exact key
tree.remove(new Point(1, 1));

// Check size
int count = tree.size();
boolean empty = tree.isEmpty();
```

### Exact Lookup

```java
// O(1) lookup via internal HashMap
String value = tree.get(new Point(0, 0));  // "origin"
```

### K-Nearest Neighbor Search

```java
Point searchKey = new Point(2, 2);

// Find single closest
SearchResult<Point, String> closest = tree.getClosest(searchKey);
Point key = closest.key();
String value = closest.value();
double distance = closest.distance();

// Find k closest
SearchResults<Point, String> results = tree.getNClosest(searchKey, 5);

for (SearchResult<Point, String> r : results.results()) {
    System.out.printf("%s at distance %.2f%n", r.value(), r.distance());
}

// Extract just keys or values
List<Point> nearbyKeys = results.keys();
List<String> nearbyValues = results.values();
List<Double> distances = results.distances();
```

### Range Search

```java
// Find all entries within distance
double maxDistance = 10.0;
SearchResults<Point, String> nearby = tree.getAllWithinRange(searchKey, maxDistance);

// Process results
nearby.stream()
    .filter(r -> r.distance() > 1.0)  // exclude very close
    .forEach(r -> process(r.value()));
```

### Rebalancing

The tree can become unbalanced with sequential insertions:

```java
// Create balanced copy (original unchanged)
MetricTree<Point, String> balanced = tree.makeBalancedCopy();

// Rebalance in place
tree.rebalance();
```

---

## MetricSet

A set optimized for spatial queries. Like MetricTree but without associated values.

### Creating a MetricSet

```java
DistanceMetric<String> editDistance = this::levenshteinDistance;
MetricSet<String> wordSet = new MetricSet<>(editDistance);
```

### Adding and Removing

```java
// Add returns true if new
boolean added = wordSet.add("hello");  // true
boolean duplicate = wordSet.add("hello");  // false

// Remove
wordSet.remove("hello");

// Check membership
boolean exists = wordSet.contains("hello");
```

### K-Nearest Neighbor Search

```java
// Find single closest
SetSearchResult<String> closest = wordSet.getClosest("helo");
String word = closest.key();
double distance = closest.distance();

// Find k closest
SetSearchResults<String> results = wordSet.getNClosest("helo", 5);

for (SetSearchResult<String> r : results.results()) {
    System.out.printf("%s (distance: %.0f)%n", r.key(), r.distance());
}

// Extract just keys
List<String> similarWords = results.keys();
```

### Range Search

```java
// Find all within edit distance of 2
SetSearchResults<String> similar = wordSet.getAllWithinRange("hello", 2.0);
```

---

## SearchResult Classes

### SearchResult<K, V> (for MetricTree)

```java
SearchResult<Point, String> result = tree.getClosest(searchKey);

Point key = result.key();
String value = result.value();
double distance = result.distance();
```

### SetSearchResult<K> (for MetricSet)

```java
SetSearchResult<String> result = wordSet.getClosest("test");

String key = result.key();
double distance = result.distance();
```

### SearchResults<K, V> (Collection)

```java
SearchResults<Point, String> results = tree.getNClosest(searchKey, 10);

// Access individual results
SearchResult<Point, String> first = results.result(0);
SearchResult<Point, String> second = results.result(1);

// Get all results as list
List<SearchResult<Point, String>> all = results.results();

// Extract components
List<Point> keys = results.keys();
List<String> values = results.values();
List<Double> distances = results.distances();

// Stream interface
results.stream()
    .filter(r -> r.distance() < 100)
    .map(SearchResult::value)
    .forEach(System.out::println);
```

---

## Distance Metrics

### Geographic Distance

```java
DistanceMetric<LatLong> geoMetric = (a, b) ->
    a.distanceTo(b).inMeters();
```

### Euclidean Distance (2D)

```java
DistanceMetric<Point> euclidean = (a, b) ->
    Math.hypot(a.x() - b.x(), a.y() - b.y());
```

### Euclidean Distance (ND)

```java
DistanceMetric<double[]> euclideanND = (a, b) -> {
    double sum = 0;
    for (int i = 0; i < a.length; i++) {
        double diff = a[i] - b[i];
        sum += diff * diff;
    }
    return Math.sqrt(sum);
};
```

### Manhattan Distance

```java
DistanceMetric<Point> manhattan = (a, b) ->
    Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
```

### Edit Distance (Strings)

```java
DistanceMetric<String> editDistance = (a, b) -> {
    int[][] dp = new int[a.length() + 1][b.length() + 1];
    for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
    for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

    for (int i = 1; i <= a.length(); i++) {
        for (int j = 1; j <= b.length(); j++) {
            int cost = a.charAt(i-1) == b.charAt(j-1) ? 0 : 1;
            dp[i][j] = Math.min(
                Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1),
                dp[i-1][j-1] + cost
            );
        }
    }
    return dp[a.length()][b.length()];
};
```

---

## CenterPointSelector

Strategy for choosing split points when tree nodes become too large.

### Built-in Selectors

```java
// Fast but potentially suboptimal (random pair)
CenterPointSelector<K> fast = CenterPointSelectors.singleRandomSample();

// Better balance (samples sqrt(n) pairs, picks most distant)
CenterPointSelector<K> balanced = CenterPointSelectors.maxOfRandomSamples();
```

### Using Custom Selector

```java
MetricTree<Point, String> tree = new MetricTree<>(
    metric,
    CenterPointSelectors.maxOfRandomSamples()
);
```

---

## Internal Structure

MetricTree uses a binary tree of spheres:

```
        [Root Sphere]
        /           \
   [Sphere A]    [Sphere B]
   /       \      /      \
[Leaf]  [Leaf] [Leaf]  [Leaf]
```

- **Leaf nodes**: Store up to N key-value pairs
- **Inner nodes**: Contain two child spheres
- **Split**: When leaf exceeds capacity, it splits into two spheres

---

## Performance Characteristics

| Operation | Complexity | Notes |
|-----------|------------|-------|
| put | O(log n) average | May trigger split |
| get | O(1) | Uses internal HashMap |
| remove | O(1) | Uses internal HashMap |
| getClosest | O(log n) to O(n) | Depends on tree balance |
| getNClosest | O(k log n) to O(n) | Depends on tree balance |
| getAllWithinRange | O(m + log n) | m = results found |
| rebalance | O(n log n) | Rebuilds entire tree |

---

## Common Patterns

### Recommendation System

```java
// Items represented as feature vectors
DistanceMetric<double[]> similarity = (a, b) -> cosineSimilarity(a, b);
MetricTree<double[], Item> items = new MetricTree<>(similarity);

// Add items
for (Item item : catalog) {
    items.put(item.featureVector(), item);
}

// Find similar items
double[] userPreferences = computeUserVector(user);
SearchResults<double[], Item> recommendations = items.getNClosest(userPreferences, 10);
```

### Spell Checker

```java
MetricSet<String> dictionary = new MetricSet<>(editDistance);
dictionary.addAll(loadDictionary());

// Check word and suggest corrections
String input = "teh";
if (!dictionary.contains(input)) {
    SetSearchResults<String> suggestions = dictionary.getNClosest(input, 5);
    System.out.println("Did you mean: " + suggestions.keys());
}
```

### Geographic Search

```java
MetricTree<LatLong, Business> businesses = new MetricTree<>(geoMetric);

// Find nearest restaurants
LatLong userLocation = getUserLocation();
SearchResults<LatLong, Business> nearby = businesses.getNClosest(userLocation, 20);

// Find all within 1km
SearchResults<LatLong, Business> walking = businesses.getAllWithinRange(userLocation, 1000);
```

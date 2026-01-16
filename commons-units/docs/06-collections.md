# Collection Utilities

Efficient storage and utilities for working with collections of locations.

## LatLongPath

Byte-efficient storage for sequences of latitude/longitude pairs. Stores locations as 8-byte longs instead of LatLong objects, providing significant memory savings for large datasets.

### Storage Efficiency

| Storage Type | Per Location | 1000 Locations |
|--------------|--------------|----------------|
| `LatLong[]` | ~24 bytes* | ~24 KB |
| `LatLongPath` | 8 bytes | 8 KB |

*Includes object overhead

### Creating LatLongPath

```java
// From array
LatLong[] locations = new LatLong[] {
    LatLong.of(40.7128, -74.0060),
    LatLong.of(40.7500, -74.0100),
    LatLong.of(40.8000, -74.0200)
};
LatLongPath path1 = LatLongPath.from(locations);

// From collection
List<LatLong> locationList = List.of(
    LatLong.of(40.7128, -74.0060),
    LatLong.of(40.7500, -74.0100),
    LatLong.of(40.8000, -74.0200)
);
LatLongPath path2 = LatLongPath.from(locationList);

// From varargs
LatLongPath path3 = LatLongPath.from(
    LatLong.of(40.7128, -74.0060),
    LatLong.of(40.7500, -74.0100)
);
```

### Accessing Locations

```java
LatLongPath path = getPath();

// By index
LatLong first = path.get(0);
LatLong last = path.get(path.size() - 1);

// Size queries
int count = path.size();
boolean empty = path.isEmpty();
```

### Iteration

```java
LatLongPath path = getPath();

// Iterator
Iterator<LatLong> iter = path.iterator();
while (iter.hasNext()) {
    LatLong loc = iter.next();
    process(loc);
}

// Enhanced for loop (implements Iterable)
for (LatLong loc : path) {
    process(loc);
}

// Stream
path.stream()
    .filter(loc -> loc.latitude() > 40.0)
    .forEach(this::process);
```

### Conversion

```java
LatLongPath path = getPath();

// To List
List<LatLong> list = path.toList();

// To Array
LatLong[] array = path.toArray();
```

### Subpaths

```java
LatLongPath fullPath = getPath();  // 100 locations

// Extract a portion (similar to List.subList)
LatLongPath segment = fullPath.subpath(10, 20);  // indices 10-19
```

### Distance Calculations

```java
LatLongPath path = getPath();

// Total distance along the path
Distance totalDist = path.pathDistance();

// Compare two paths (sum of differences at each index)
LatLongPath path1 = getPath1();
LatLongPath path2 = getPath2();  // Must be same size
Distance difference = LatLongPath.distanceBtw(path1, path2);
```

### Serialization

```java
LatLongPath path = getPath();

// Binary serialization (8 bytes per location)
byte[] bytes = path.toBytes();
// Deserialize: LatLongPath.fromBytes(bytes)

// Base64 encoding
String base64 = path.toBase64();
// Deserialize: LatLongPath.fromBase64(base64)
```

---

## CollectionUtils

Binary search utility for sorted lists where the search key is a derived value.

### Problem It Solves

Standard `Collections.binarySearch()` requires the search key to be the same type as list elements. `CollectionUtils.binarySearch()` allows searching by an extracted feature.

### Basic Usage

```java
// List of positions sorted by time
List<Position> positions = getSortedPositions();

// Search by time (not by Position)
Instant searchTime = Instant.parse("2024-01-01T12:00:00Z");

int index = CollectionUtils.binarySearch(
    positions,
    Position::time,      // Feature extractor
    searchTime           // Search key
);

// Standard binary search contract:
// - If found: returns index of matching element
// - If not found: returns -(insertion point) - 1
```

### Finding Insertion Point

```java
List<Position> positions = getSortedPositions();
Instant searchTime = Instant.now();

int result = CollectionUtils.binarySearch(positions, Position::time, searchTime);

if (result >= 0) {
    // Exact match found at index 'result'
    Position exact = positions.get(result);
} else {
    // Not found, calculate insertion point
    int insertionPoint = -(result + 1);
    // insertionPoint is where searchTime would be inserted to maintain order
}
```

### Floor and Ceiling

```java
List<Position> positions = getSortedPositions();
Instant searchTime = Instant.now();

int result = CollectionUtils.binarySearch(positions, Position::time, searchTime);

// Find floor (latest element <= searchTime)
Position floor = null;
if (result >= 0) {
    floor = positions.get(result);
} else {
    int insertionPoint = -(result + 1);
    if (insertionPoint > 0) {
        floor = positions.get(insertionPoint - 1);
    }
}

// Find ceiling (earliest element >= searchTime)
Position ceiling = null;
if (result >= 0) {
    ceiling = positions.get(result);
} else {
    int insertionPoint = -(result + 1);
    if (insertionPoint < positions.size()) {
        ceiling = positions.get(insertionPoint);
    }
}
```

---

## Common Patterns

### Building Efficient Tracks

```java
// Collect GPS readings efficiently
List<LatLong> readings = new ArrayList<>();

while (tracking) {
    LatLong current = gps.getCurrentLocation();
    readings.add(current);
}

// Convert to efficient storage
LatLongPath track = LatLongPath.from(readings);

// Serialize for storage
byte[] data = track.toBytes();
saveToDatabase(data);
```

### Route Analysis

```java
LatLongPath route = getRoute();

// Calculate total distance
Distance totalDist = route.pathDistance();

// Calculate segment distances
List<Distance> segments = new ArrayList<>();
for (int i = 1; i < route.size(); i++) {
    Distance segDist = route.get(i-1).distanceTo(route.get(i));
    segments.add(segDist);
}

// Find longest segment
Distance longest = segments.stream()
    .max(Comparator.comparing(Distance::inMeters))
    .orElse(Distance.ofMeters(0));
```

### Path Comparison

```java
LatLongPath planned = getPlannedRoute();
LatLongPath actual = getActualRoute();

// Compare paths point-by-point
Distance totalDeviation = LatLongPath.distanceBtw(planned, actual);

// Find maximum deviation
Distance maxDeviation = Distance.ofMeters(0);
for (int i = 0; i < planned.size(); i++) {
    Distance deviation = planned.get(i).distanceTo(actual.get(i));
    if (deviation.isGreaterThan(maxDeviation)) {
        maxDeviation = deviation;
    }
}
```

### Time-Based Lookups

```java
// Positions sorted by time
List<Position> timeline = getSortedTimeline();

// Efficient time-based queries
Instant queryTime = Instant.parse("2024-01-01T12:00:00Z");

// Using HasTime utilities (built on CollectionUtils)
Position atTime = HasTime.closest(timeline, queryTime);
Position before = HasTime.floor(timeline, queryTime);
Position after = HasTime.ceiling(timeline, queryTime);
```

### Spatial Indexing (Simple)

```java
// Group locations by grid cell for simple spatial queries
Map<String, List<LatLong>> grid = new HashMap<>();
double cellSize = 0.1;  // degrees

for (LatLong loc : allLocations) {
    int latCell = (int)(loc.latitude() / cellSize);
    int lonCell = (int)(loc.longitude() / cellSize);
    String key = latCell + "," + lonCell;

    grid.computeIfAbsent(key, k -> new ArrayList<>()).add(loc);
}

// Query nearby cells
LatLong query = LatLong.of(40.7128, -74.0060);
int qLatCell = (int)(query.latitude() / cellSize);
int qLonCell = (int)(query.longitude() / cellSize);

List<LatLong> nearby = new ArrayList<>();
for (int dLat = -1; dLat <= 1; dLat++) {
    for (int dLon = -1; dLon <= 1; dLon++) {
        String key = (qLatCell + dLat) + "," + (qLonCell + dLon);
        nearby.addAll(grid.getOrDefault(key, List.of()));
    }
}
```

### Downsampling Paths

```java
LatLongPath fullPath = getHighResolutionPath();  // 10000 points

// Downsample to every Nth point
int sampleRate = 10;
List<LatLong> sampled = new ArrayList<>();
for (int i = 0; i < fullPath.size(); i += sampleRate) {
    sampled.add(fullPath.get(i));
}
// Always include last point
if ((fullPath.size() - 1) % sampleRate != 0) {
    sampled.add(fullPath.get(fullPath.size() - 1));
}

LatLongPath downsampled = LatLongPath.from(sampled);
```

### Path Simplification (Douglas-Peucker style)

```java
// Simplify path by removing points that don't significantly affect shape
Distance tolerance = Distance.ofMeters(10);

List<LatLong> simplified = new ArrayList<>();
simplified.add(path.get(0));  // Always keep first

int lastKept = 0;
for (int i = 1; i < path.size() - 1; i++) {
    // Check if point is far enough from line between lastKept and end
    LatLong start = path.get(lastKept);
    LatLong end = path.get(path.size() - 1);
    LatLong current = path.get(i);

    // Simplified perpendicular distance check
    Distance directDist = start.distanceTo(end);
    Distance viaPoint = start.distanceTo(current).plus(current.distanceTo(end));
    Distance deviation = viaPoint.minus(directDist);

    if (deviation.isGreaterThan(tolerance)) {
        simplified.add(current);
        lastKept = i;
    }
}

simplified.add(path.get(path.size() - 1));  // Always keep last
LatLongPath simplifiedPath = LatLongPath.from(simplified);
```

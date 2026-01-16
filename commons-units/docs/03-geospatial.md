# Geospatial Classes

Classes for working with geographic coordinates and navigation calculations.

## LatLong (Compressed)

Space-efficient, immutable latitude/longitude pair using 32-bit integer storage.

### Storage Characteristics

| Property | Value |
|----------|-------|
| Storage Size | 8 bytes (2x 32-bit integers) |
| Accuracy | ~7 decimal places (~11mm resolution) |
| Compression | 50% smaller than double-based storage |

### Creating LatLong

```java
// From latitude and longitude
LatLong nyc = LatLong.of(40.7128, -74.0060);
LatLong london = LatLong.of(51.5074, -0.1278);
LatLong tokyo = LatLong.of(35.6762, 139.6503);

// From bytes (deserialization)
byte[] data = nyc.toBytes();
LatLong restored = LatLong.fromBytes(data);

// From Base64 string
String encoded = nyc.toBase64();           // 11 characters
LatLong decoded = LatLong.fromBase64Str(encoded);
```

### Accessing Coordinates

```java
LatLong loc = LatLong.of(40.7128, -74.0060);

double lat = loc.latitude();               // 40.7128
double lon = loc.longitude();              // -74.0060
```

### Distance and Course Calculations

```java
LatLong nyc = LatLong.of(40.7128, -74.0060);
LatLong la = LatLong.of(34.0522, -118.2437);

// Calculate distance between points
Distance distance = nyc.distanceTo(la);
double nm = distance.inNauticalMiles();    // ~2,128 NM

// Calculate bearing/course to another point
Course bearing = nyc.courseTo(la);
double degrees = bearing.inDegrees();      // ~273° (westward)
```

### Projecting New Locations

```java
LatLong start = LatLong.of(40.7128, -74.0060);

// Move in a direction by a distance
Course heading = Course.ofDegrees(270);    // West
Distance dist = Distance.ofNauticalMiles(100);

LatLong destination = start.move(heading, dist);
```

### Proximity Checks

```java
LatLong point = LatLong.of(40.7128, -74.0060);
LatLong center = LatLong.of(40.7500, -74.0000);
Distance radius = Distance.ofNauticalMiles(10);

// Check if point is within radius of center
boolean nearby = point.isWithin(radius, center.latLong128());
```

### Serialization

```java
LatLong loc = LatLong.of(40.7128, -74.0060);

// Binary serialization (8 bytes)
byte[] bytes = loc.toBytes();
LatLong fromBytes = LatLong.fromBytes(bytes);

// Base64 encoding (11 characters)
String base64 = loc.toBase64();            // e.g., "aHpvnvpRj8Y"
LatLong fromB64 = LatLong.fromBase64Str(base64);
```

### Converting to Full Precision

```java
LatLong compressed = LatLong.of(40.7128, -74.0060);
LatLong128 full = compressed.inflate();    // Full double precision
```

---

## LatLong128 (Full Precision)

Immutable latitude/longitude pair with full 64-bit double precision.

### Storage Characteristics

| Property | Value |
|----------|-------|
| Storage Size | 16 bytes (2x 64-bit doubles) |
| Accuracy | Full double precision |
| Use Case | When precision beyond 7 decimals is needed |

### Creating LatLong128

```java
// From latitude and longitude
LatLong128 precise = LatLong128.of(40.71277777777778, -74.00597222222222);

// From LatLong (inflation)
LatLong compressed = LatLong.of(40.7128, -74.0060);
LatLong128 inflated = compressed.inflate();
```

### All LatLong Operations

`LatLong128` supports the same operations as `LatLong`:

```java
LatLong128 loc1 = LatLong128.of(40.7128, -74.0060);
LatLong128 loc2 = LatLong128.of(34.0522, -118.2437);

// Distance and course
Distance distance = loc1.distanceTo(loc2);
Course bearing = loc1.courseTo(loc2);

// Movement
LatLong128 destination = loc1.move(Course.WEST, Distance.ofNauticalMiles(100));

// Proximity
boolean nearby = loc1.isWithin(Distance.ofMiles(10), loc2);
```

### Converting to Compressed

```java
LatLong128 precise = LatLong128.of(40.71277777777778, -74.00597222222222);
LatLong compressed = precise.compress();   // Loses precision after 7 decimals
```

### Validation

```java
// Static validation methods
LatLong128.checkLatitude(91.0);    // Throws: latitude out of range
LatLong128.checkLongitude(181.0);  // Throws: longitude out of range
```

---

## HasLatLong Interface

Common interface implemented by all location-aware classes.

### Implementations

- `LatLong`
- `LatLong128`
- `Position`
- `KineticPosition`

### Interface Methods

```java
public interface HasLatLong {
    double latitude();
    double longitude();

    default LatLong latLong() { ... }
    default LatLong128 latLong128() { ... }

    default Distance distanceTo(HasLatLong that) { ... }
    default Course courseInDegrees(HasLatLong that) { ... }
    default boolean isWithin(Distance distance, LatLong128 location) { ... }
    default LatLong128 move(Course direction, Distance distance) { ... }
}
```

### Polymorphic Usage

```java
// Any HasLatLong implementation works
void processLocation(HasLatLong location) {
    double lat = location.latitude();
    double lon = location.longitude();

    LatLong target = LatLong.of(40.7128, -74.0060);
    Distance dist = location.distanceTo(target);
    Course bearing = location.courseInDegrees(target);
}

// Call with any implementation
processLocation(LatLong.of(34.0522, -118.2437));
processLocation(LatLong128.of(34.0522, -118.2437));
processLocation(somePosition);
processLocation(someKineticPosition);
```

### Averaging Locations

```java
List<LatLong> locations = List.of(
    LatLong.of(40.7128, -74.0060),
    LatLong.of(40.7500, -74.0000),
    LatLong.of(40.7300, -74.0100)
);

// Accurate average (for small areas)
LatLong128 avgAccurate = HasLatLong.avgLatLong(locations);

// Quick average (simple arithmetic mean)
LatLong128 avgQuick = HasLatLong.quickAvgLatLong(locations);
```

---

## Navigation Utilities

Static utility class for advanced geospatial calculations using great circle math.

### Distance Calculations

```java
// Distance in nautical miles
double distNM = Navigation.distanceInNM(
    40.7128, -74.0060,    // NYC
    34.0522, -118.2437    // LA
);

// Distance as Distance object
Distance dist = Navigation.distanceBtw(
    40.7128, -74.0060,
    34.0522, -118.2437
);
```

### Bearing Calculations

```java
// Initial bearing between two points
Course bearing = Navigation.courseBtw(
    40.7128, -74.0060,    // From: NYC
    34.0522, -118.2437    // To: LA
);

double degrees = Navigation.courseInDegrees(
    40.7128, -74.0060,
    34.0522, -118.2437
);
```

### Point Projection

```java
// Project a point from a starting location
LatLong128 destination = Navigation.move(
    40.7128, -74.0060,    // Start
    270.0,                // Heading (degrees)
    100.0                 // Distance (nautical miles)
);

// With curvature (turning while moving)
double curvatureNM = 50.0;  // Turn radius
LatLong128 curved = Navigation.move(
    40.7128, -74.0060,
    270.0,
    100.0,
    curvatureNM
);
```

### Turn Calculations

```java
// Find the center of a turn
LatLong128 origin = Navigation.greatCircleOrigin(
    40.7128, -74.0060,    // Current position
    270.0,                // Current heading
    50.0                  // Turn radius (NM)
);

// Calculate curvature between two points
double curvature = Navigation.curvatureFromPointToPoint(
    lat1, lon1, course1,
    lat2, lon2
);
```

### Angle Normalization

```java
// Normalize angle difference (handles 0-360 wraparound)
double diff1 = Navigation.angleDifference(350, 10);   // 20
double diff2 = Navigation.angleDifference(10, 350);   // -20
```

### Constants

```java
double earthRadiusNM = Navigation.EARTH_RADIUS_NM;    // 3440.065
double metersPerNM = Navigation.METERS_PER_NM;        // 1852.0
double metersPerFoot = Navigation.METERS_PER_FOOT;    // 0.3048
```

---

## Common Patterns

### Route Distance Calculation

```java
List<LatLong> waypoints = List.of(
    LatLong.of(40.7128, -74.0060),   // NYC
    LatLong.of(41.8781, -87.6298),   // Chicago
    LatLong.of(34.0522, -118.2437)   // LA
);

Distance totalDistance = Distance.ofMeters(0);
for (int i = 0; i < waypoints.size() - 1; i++) {
    totalDistance = totalDistance.plus(
        waypoints.get(i).distanceTo(waypoints.get(i + 1))
    );
}
```

### Finding Nearest Location

```java
LatLong target = LatLong.of(40.7128, -74.0060);
List<LatLong> candidates = List.of(/* ... */);

LatLong nearest = candidates.stream()
    .min(Comparator.comparing(loc -> loc.distanceTo(target).inMeters()))
    .orElse(null);
```

### Geofencing

```java
LatLong center = LatLong.of(40.7128, -74.0060);
Distance radius = Distance.ofMiles(5);

boolean insideGeofence(HasLatLong location) {
    return location.isWithin(radius, center.latLong128());
}
```

### Bounding Box Check

```java
double minLat = 40.0, maxLat = 41.0;
double minLon = -75.0, maxLon = -73.0;

boolean inBounds(LatLong loc) {
    return loc.latitude() >= minLat && loc.latitude() <= maxLat
        && loc.longitude() >= minLon && loc.longitude() <= maxLon;
}
```

### Compression for Storage

```java
// Store thousands of locations efficiently
List<LatLong128> preciseLocations = getPreciseLocations();

// Compress for storage (saves 50%)
List<LatLong> compressed = preciseLocations.stream()
    .map(LatLong128::compress)
    .toList();

// Serialize to bytes
byte[][] allBytes = compressed.stream()
    .map(LatLong::toBytes)
    .toArray(byte[][]::new);
// Each entry is exactly 8 bytes
```

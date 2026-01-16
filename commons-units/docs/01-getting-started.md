# Commons Units - Getting Started

A type-safe Java library for working with physical measurements, geospatial coordinates, and temporal data. Designed for aviation, maritime, logistics, and geospatial applications.

## Overview

Commons Units provides immutable, type-safe representations of:

| Category | Classes |
|----------|---------|
| **Measurements** | `Distance`, `Speed`, `Acceleration`, `Course` |
| **Geospatial** | `LatLong`, `LatLong128`, `Navigation` |
| **Temporal** | `Time`, `TimeWindow` |
| **Positions** | `Position`, `KineticPosition` |
| **Collections** | `LatLongPath` |

## Key Features

- **Immutable classes** - Thread-safe and predictable
- **Type-safe units** - Enums prevent unit confusion
- **Automatic unit conversion** - Cross-unit operations handled transparently
- **Compact serialization** - Binary and Base64 encodings
- **High performance** - Uses Apache Commons Math FastMath (2.23x faster trig)

## Package Structure

```
io.github.jon1van.units
├── Distance          # Distance with unit support (meters, feet, NM, etc.)
├── Speed             # Speed/velocity (knots, m/s, mph, etc.)
├── Acceleration      # Rate of speed change
├── Course            # Heading/bearing in degrees or radians
├── LatLong           # Compressed lat/long (8 bytes, ~11mm accuracy)
├── LatLong128        # Full precision lat/long (16 bytes)
├── Navigation        # Great circle calculations
├── Time              # Time utilities and comparisons
├── TimeWindow        # Fixed time intervals
├── Position          # 3D/4D location (time + lat/long + optional altitude)
├── KineticPosition   # Position with kinetic properties (speed, course, etc.)
├── LatLongPath       # Efficient storage for many locations
└── CollectionUtils   # Binary search utilities
```

## Quick Examples

### Working with Distances

```java
// Create distances in different units
Distance d1 = Distance.ofMeters(1000);
Distance d2 = Distance.ofNauticalMiles(5);
Distance d3 = Distance.ofFeet(3280.84);

// Convert between units
double meters = d2.inMeters();          // 9260.0
double nm = d1.inNauticalMiles();       // 0.5399...

// Arithmetic operations
Distance total = d1.plus(d2);
Distance half = d1.times(0.5);

// Comparisons work across units
boolean isLess = d1.isLessThan(d2);     // true
```

### Working with Locations

```java
// Create locations
LatLong nyc = LatLong.of(40.7128, -74.0060);
LatLong la = LatLong.of(34.0522, -118.2437);

// Calculate distance and bearing
Distance distance = nyc.distanceTo(la);
Course bearing = nyc.courseTo(la);

// Project a new location
LatLong destination = nyc.move(Course.ofDegrees(270), Distance.ofNauticalMiles(100));

// Compact serialization
byte[] bytes = nyc.toBytes();    // 8 bytes
String b64 = nyc.toBase64();     // 11 characters
```

### Working with Speed and Time

```java
// Create speeds
Speed cruise = Speed.ofKnots(450);
Speed highway = Speed.ofMilesPerHour(65);

// Calculate travel time and distance
Distance traveled = cruise.times(Duration.ofHours(2));
Duration eta = cruise.timeToTravel(Distance.ofNauticalMiles(1000));

// Time windows
TimeWindow window = TimeWindow.of(startTime, endTime);
boolean contains = window.contains(Instant.now());

// Iterate through time
for (Instant t : window.steppedIteration(Duration.ofMinutes(5))) {
    // Process each 5-minute interval
}
```

### Working with Positions

```java
// Create a position with time and location
Position pos = Position.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .altitude(Distance.ofFeet(35000))
    .build();

// Create a kinetic position with movement data
KineticPosition kpos = KineticPosition.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .altitude(Distance.ofFeet(35000))
    .speed(Speed.ofKnots(450))
    .course(Course.ofDegrees(270))
    .climbRate(Speed.ofFeetPerMinute(0))
    .build();

// Serialize to bytes
byte[] data = kpos.toBytes();    // 72 bytes
```

## Design Principles

1. **Immutability** - All value classes are immutable
2. **Type Safety** - Units are enforced at compile time
3. **Null Safety** - Factory methods validate inputs
4. **Precision Control** - Choose between compressed and full-precision types
5. **Interoperability** - Binary/Base64 serialization for cross-language use

## Dependencies

- Apache Commons Math3 (3.6.1) - Fast trigonometric functions
- Google Guava - Preconditions and utilities

## Next Steps

- [Measurement Units](02-measurement-units.md) - Distance, Speed, Acceleration, Course
- [Geospatial](03-geospatial.md) - LatLong, Navigation
- [Temporal](04-temporal.md) - Time, TimeWindow
- [Positions](05-positions.md) - Position, KineticPosition
- [Collections](06-collections.md) - LatLongPath, CollectionUtils

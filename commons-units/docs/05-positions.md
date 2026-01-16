# Position Classes

Classes for representing locations with temporal and kinetic properties.

## Position

Immutable 3D or 4D location combining time, latitude, longitude, and optional altitude.

### Interfaces Implemented

- `HasTime` - Provides time-based operations
- `HasLatLong` - Provides location-based operations

### Creating Positions

#### Using the Builder (Recommended)

```java
// Basic position with time and location
Position pos1 = Position.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .build();

// Position with altitude
Position pos2 = Position.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .altitude(Distance.ofFeet(35000))
    .build();

// Using LatLong object
LatLong nyc = LatLong.of(40.7128, -74.0060);
Position pos3 = Position.builder()
    .time(Instant.now())
    .latLong(nyc)
    .build();
```

#### Using Constructors

```java
// With LatLong
Position pos1 = new Position(Instant.now(), LatLong.of(40.7128, -74.0060));

// With LatLong and altitude
Position pos2 = new Position(
    Instant.now(),
    LatLong.of(40.7128, -74.0060),
    Distance.ofFeet(35000)
);

// With raw coordinates
Position pos3 = new Position(Instant.now(), 40.7128, -74.0060);

// With raw coordinates and altitude
Position pos4 = new Position(
    Instant.now(),
    40.7128, -74.0060,
    Distance.ofFeet(35000)
);
```

### Accessing Properties

```java
Position pos = Position.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .altitude(Distance.ofFeet(35000))
    .build();

// Time access (from HasTime)
Instant time = pos.time();
long epochMs = pos.timeAsEpochMs();

// Location access (from HasLatLong)
double lat = pos.latitude();
double lon = pos.longitude();
LatLong latLong = pos.latLong();
LatLong128 precise = pos.latLong128();

// Altitude access
Distance alt = pos.altitude();      // May be null if not set
boolean hasAlt = pos.hasAltitude(); // Check if altitude is present
```

### Location Operations

Since `Position` implements `HasLatLong`, all geospatial operations are available:

```java
Position pos1 = Position.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .build();

Position pos2 = Position.builder()
    .time(Instant.now().plusSeconds(3600))
    .latLong(40.8000, -74.1000)
    .build();

// Distance between positions
Distance dist = pos1.distanceTo(pos2);

// Course/bearing to another position
Course bearing = pos1.courseInDegrees(pos2);

// Check proximity
boolean nearby = pos1.isWithin(Distance.ofMiles(10), pos2.latLong128());

// Project new location
LatLong128 destination = pos1.move(Course.WEST, Distance.ofNauticalMiles(50));
```

### Time Operations

Since `Position` implements `HasTime`, all temporal operations are available:

```java
Position pos1 = Position.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .build();

Position pos2 = Position.builder()
    .time(Instant.now().plusSeconds(3600))
    .latLong(40.8000, -74.1000)
    .build();

// Duration between positions
Duration elapsed = pos1.durationBtw(pos2);

// Binary search in sorted list
List<Position> positions = getSortedPositions();
Position closest = HasTime.closest(positions, targetTime);
```

---

## KineticPosition

Immutable 4D position with full kinetic properties: speed, acceleration, course, turn rate, and climb rate.

### Storage Size

72 bytes total:
- 8 bytes: epoch time (long)
- 24 bytes: latitude, longitude, altitude (3x double)
- 40 bytes: climbRate, course, turnRate, speed, acceleration (5x double)

### Interfaces Implemented

- `HasTime` - Provides time-based operations
- `HasLatLong` - Provides location-based operations

### Creating KineticPositions

#### Using the Builder (Recommended)

```java
// Full kinetic position
KineticPosition kpos = KineticPosition.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .altitude(Distance.ofFeet(35000))
    .speed(Speed.ofKnots(450))
    .course(Course.ofDegrees(270))
    .acceleration(Acceleration.of(Speed.ofKnots(0)))
    .turnRate(0.0)                          // degrees per second
    .climbRate(Speed.ofFeetPerMinute(0))
    .build();

// Minimal kinetic position
KineticPosition kpos2 = KineticPosition.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .speed(Speed.ofKnots(450))
    .course(Course.WEST)
    .build();
```

#### Using Constructor

```java
Position basePos = Position.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .altitude(Distance.ofFeet(35000))
    .build();

KineticPosition kpos = new KineticPosition(
    basePos,
    Speed.ofKnots(450),
    Acceleration.of(Speed.ofKnots(0)),
    Course.ofDegrees(270),
    0.0,                                    // turn rate (deg/sec)
    Speed.ofFeetPerMinute(0)                // climb rate
);
```

### Accessing Properties

```java
KineticPosition kpos = getKineticPosition();

// Position properties (from HasTime and HasLatLong)
Instant time = kpos.time();
double lat = kpos.latitude();
double lon = kpos.longitude();
Distance alt = kpos.altitude();

// Kinetic properties
Speed speed = kpos.speed();
Speed climbRate = kpos.climbRate();
Acceleration accel = kpos.acceleration();
Course course = kpos.course();
double turnRate = kpos.turnRate();         // degrees per second
```

### Turn Radius Calculation

```java
KineticPosition kpos = KineticPosition.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .speed(Speed.ofKnots(200))
    .course(Course.NORTH)
    .turnRate(3.0)                          // 3 degrees per second
    .build();

// Calculate turn radius from speed and turn rate
Distance radius = kpos.turnRadius();
// Result: nautical miles needed to complete the turn
```

### Serialization

```java
KineticPosition kpos = getKineticPosition();

// Binary serialization (72 bytes)
byte[] bytes = kpos.toBytes();
// Note: fromBytes() would be used to deserialize

// Base64 encoding (96 characters)
String base64 = kpos.toBase64();
```

---

## Builder Patterns

Both `Position` and `KineticPosition` use builders with two types of setters:

### Enforce New vs Override

```java
// "Enforce new" setters (default) - throw if already set
Position.builder()
    .time(time1)
    .time(time2)  // Throws! Time already set
    .build();

// "Override" setters - allow replacing values
Position.builder()
    .time(time1)
    .butWithTime(time2)  // OK, replaces time1
    .build();
```

### Available Builder Methods

#### Position.Builder

| Enforce New | Override | Description |
|-------------|----------|-------------|
| `time(Instant)` | `butWithTime(Instant)` | Set timestamp |
| `latLong(double, double)` | `butWithLatLong(double, double)` | Set coordinates |
| `latLong(LatLong)` | `butWithLatLong(LatLong)` | Set from LatLong |
| `latLong(LatLong128)` | `butWithLatLong(LatLong128)` | Set from LatLong128 |
| `altitude(Distance)` | `butWithAltitude(Distance)` | Set altitude |

#### KineticPosition.Builder

Includes all Position.Builder methods plus:

| Enforce New | Override | Description |
|-------------|----------|-------------|
| `speed(Speed)` | `butWithSpeed(Speed)` | Set speed |
| `course(Course)` | `butWithCourse(Course)` | Set heading |
| `acceleration(Acceleration)` | `butWithAcceleration(Acceleration)` | Set acceleration |
| `turnRate(double)` | `butWithTurnRate(double)` | Set turn rate (deg/sec) |
| `climbRate(Speed)` | `butWithClimbRate(Speed)` | Set vertical speed |

---

## Common Patterns

### Tracking Movement

```java
List<Position> track = new ArrayList<>();
Instant startTime = Instant.now();

// Record positions over time
for (int i = 0; i < 100; i++) {
    LatLong currentLoc = getCurrentLocation();
    Position pos = Position.builder()
        .time(startTime.plusSeconds(i))
        .latLong(currentLoc)
        .build();
    track.add(pos);
}

// Calculate total distance traveled
Distance total = Distance.ofMeters(0);
for (int i = 1; i < track.size(); i++) {
    total = total.plus(track.get(i-1).distanceTo(track.get(i)));
}

// Calculate average speed
Duration elapsed = track.get(0).durationBtw(track.get(track.size()-1));
Speed avgSpeed = new Speed(total, elapsed);
```

### Interpolating Positions

```java
Position p1 = getPosition1();
Position p2 = getPosition2();
Instant targetTime = getTargetTime();

// Linear interpolation
TimeWindow window = TimeWindow.of(p1.time(), p2.time());
double fraction = window.toFractionOfRange(targetTime);

double interpLat = p1.latitude() + fraction * (p2.latitude() - p1.latitude());
double interpLon = p1.longitude() + fraction * (p2.longitude() - p1.longitude());

Position interpolated = Position.builder()
    .time(targetTime)
    .latLong(interpLat, interpLon)
    .build();
```

### Flight State Representation

```java
// Represent aircraft state
KineticPosition aircraft = KineticPosition.builder()
    .time(Instant.now())
    .latLong(40.7128, -74.0060)
    .altitude(Distance.ofFeet(35000))
    .speed(Speed.ofKnots(450))
    .course(Course.ofDegrees(270))           // Heading west
    .climbRate(Speed.ofFeetPerMinute(-500))  // Descending
    .turnRate(0.0)                            // Flying straight
    .acceleration(Acceleration.of(Speed.ofKnots(-2)))  // Slowing down
    .build();

// Project future position
Duration lookahead = Duration.ofMinutes(5);
Distance traveled = aircraft.speed().times(lookahead);
LatLong128 futurePos = aircraft.move(aircraft.course(), traveled);

// Calculate altitude change
Distance altChange = aircraft.climbRate().times(lookahead);
Distance futureAlt = aircraft.altitude().plus(altChange);
```

### Sorting and Searching

```java
List<Position> positions = getPositions();

// Sort by time
positions.sort(Time.compareByTime());

// Find position at specific time
Instant searchTime = Instant.parse("2024-01-01T12:00:00Z");
Position closest = HasTime.closest(positions, searchTime);
Position floor = HasTime.floor(positions, searchTime);
Position ceiling = HasTime.ceiling(positions, searchTime);

// Binary search for index
int index = HasTime.binarySearch(positions, searchTime);
```

### Converting Between Types

```java
// Position to KineticPosition (add kinetic data)
Position pos = getPosition();
KineticPosition kpos = KineticPosition.builder()
    .time(pos.time())
    .latLong(pos.latLong())
    .altitude(pos.altitude())
    .speed(calculatedSpeed)
    .course(calculatedCourse)
    .build();

// KineticPosition to Position (strip kinetic data)
KineticPosition kpos = getKineticPosition();
Position pos = Position.builder()
    .time(kpos.time())
    .latLong(kpos.latitude(), kpos.longitude())
    .altitude(kpos.altitude())
    .build();
```

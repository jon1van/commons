# Measurement Units

Type-safe classes for physical measurements: Distance, Speed, Acceleration, and Course.

## Distance

Immutable representation of a distance with automatic unit conversion.

### Supported Units

| Unit | Enum Constant | Description |
|------|---------------|-------------|
| Kilometers | `KILOMETERS` | Metric |
| Meters | `METERS` | Metric (base unit) |
| Feet | `FEET` | Imperial |
| Miles | `MILES` | Imperial |
| Nautical Miles | `NAUTICAL_MILES` | Aviation/Maritime |

### Creating Distances

```java
// Using factory methods (preferred)
Distance d1 = Distance.ofMeters(1000);
Distance d2 = Distance.ofFeet(3280.84);
Distance d3 = Distance.ofNauticalMiles(5);
Distance d4 = Distance.ofKilometers(10);
Distance d5 = Distance.ofMiles(1);

// Using generic factory with unit
Distance d6 = Distance.of(100, Distance.Unit.METERS);

// Parsing from strings
Distance d7 = Distance.fromString("100nm");    // nautical miles
Distance d8 = Distance.fromString("50.5m");    // meters
Distance d9 = Distance.fromString("1000ft");   // feet
```

### Unit Conversion

```java
Distance d = Distance.ofNauticalMiles(1);

// Convert to specific units
double meters = d.inMeters();              // 1852.0
double feet = d.inFeet();                  // 6076.12
double km = d.inKilometers();              // 1.852
double miles = d.inMiles();                // 1.15078

// Generic conversion
double value = d.in(Distance.Unit.FEET);   // 6076.12
```

### Arithmetic Operations

```java
Distance d1 = Distance.ofMeters(100);
Distance d2 = Distance.ofFeet(328);        // ~100 meters

// Addition and subtraction (handles unit conversion)
Distance sum = d1.plus(d2);                // ~200 meters
Distance diff = d1.minus(d2);              // ~0 meters

// Scalar multiplication
Distance doubled = d1.times(2);            // 200 meters
Distance half = d1.times(0.5);             // 50 meters

// Division by duration returns Speed
Speed velocity = d1.dividedBy(Duration.ofSeconds(10));  // 10 m/s
```

### Comparisons

```java
Distance d1 = Distance.ofMeters(1000);
Distance d2 = Distance.ofNauticalMiles(0.5);  // ~926 meters

// Comparison methods (work across units)
boolean less = d1.isLessThan(d2);          // false
boolean greater = d1.isGreaterThan(d2);    // true
boolean lte = d1.isLessThanOrEqualTo(d2);  // false
boolean gte = d1.isGreaterThanOrEqualTo(d2); // true

// Using Comparable
int cmp = d1.compareTo(d2);                // positive (d1 > d2)
```

### Aggregation

```java
Distance d1 = Distance.ofMeters(100);
Distance d2 = Distance.ofMeters(200);
Distance d3 = Distance.ofMeters(300);

// Static aggregation methods
Distance total = Distance.sum(d1, d2, d3);     // 600 meters
Distance avg = Distance.mean(d1, d2, d3);      // 200 meters
Distance minimum = Distance.min(d1, d2, d3);   // 100 meters
Distance maximum = Distance.max(d1, d2, d3);   // 300 meters
```

### String Representation

```java
Distance d = Distance.ofNauticalMiles(5.5);

String str = d.toString();                 // "5.5 [NAUTICAL_MILES]"
Distance parsed = Distance.fromString("5.5nm");
```

---

## Speed

Immutable representation of velocity (distance per time).

### Supported Units

| Unit | Enum Constant | Description |
|------|---------------|-------------|
| Knots | `KNOTS` | Nautical miles per hour |
| Meters/Second | `METERS_PER_SECOND` | SI unit |
| Feet/Second | `FEET_PER_SECOND` | Imperial |
| Feet/Minute | `FEET_PER_MINUTE` | Aviation (climb rate) |
| Miles/Hour | `MILES_PER_HOUR` | Common US |
| Kilometers/Hour | `KILOMETERS_PER_HOUR` | Common metric |

### Creating Speeds

```java
// Using factory methods
Speed s1 = Speed.ofKnots(450);
Speed s2 = Speed.ofMetersPerSecond(10);
Speed s3 = Speed.ofMilesPerHour(65);
Speed s4 = Speed.ofFeetPerMinute(500);
Speed s5 = Speed.ofKilometersPerHour(100);

// Using generic factory with unit
Speed s6 = Speed.of(100, Speed.Unit.KNOTS);

// From distance and duration
Distance dist = Distance.ofNauticalMiles(100);
Duration time = Duration.ofHours(1);
Speed s7 = new Speed(dist, time);          // 100 knots

// Calculate speed between two positions
Speed s8 = Speed.between(latLong1, time1, latLong2, time2);
```

### Unit Conversion

```java
Speed s = Speed.ofKnots(100);

// Convert to specific units
double mps = s.inMetersPerSecond();        // 51.44
double mph = s.inMilesPerHour();           // 115.08
double kph = s.inKilometersPerHour();      // 185.2
double fps = s.inFeetPerSecond();          // 168.78
double fpm = s.inFeetPerMinute();          // 10126.86

// Generic conversion
double value = s.in(Speed.Unit.MILES_PER_HOUR);
```

### Travel Calculations

```java
Speed cruise = Speed.ofKnots(450);

// Distance traveled in a given time
Distance traveled = cruise.times(Duration.ofHours(2));  // 900 NM

// Time to travel a given distance
Duration eta = cruise.timeToTravel(Distance.ofNauticalMiles(1000));
// Result: ~2 hours 13 minutes
```

### Arithmetic Operations

```java
Speed s1 = Speed.ofKnots(100);
Speed s2 = Speed.ofKnots(50);

// Addition and subtraction
Speed sum = s1.plus(s2);                   // 150 knots
Speed diff = s1.minus(s2);                 // 50 knots

// Scalar multiplication
Speed doubled = s1.times(2);               // 200 knots
```

### Unit Matching

```java
// Get appropriate speed unit for a distance unit
Speed.Unit speedUnit = Speed.speedUnitFor(Distance.Unit.NAUTICAL_MILES);
// Returns: Speed.Unit.KNOTS

Speed.Unit speedUnit2 = Speed.speedUnitFor(Distance.Unit.FEET);
// Returns: Speed.Unit.FEET_PER_SECOND
```

---

## Acceleration

Immutable representation of rate of speed change over time.

### Creating Accelerations

```java
// From speed change over time
Speed deltaV = Speed.ofKnots(50);
Duration time = Duration.ofSeconds(10);
Acceleration accel = new Acceleration(deltaV, time);  // 5 knots/sec

// From normalized speed change per second
Speed perSecond = Speed.ofKnots(2);
Acceleration accel2 = Acceleration.of(perSecond);     // 2 knots/sec
```

### Using Accelerations

```java
Acceleration accel = Acceleration.of(Speed.ofKnots(2));

// Get the speed change per second
Speed deltaPerSec = accel.speedDeltaPerSecond();      // 2 knots/sec

// Scale acceleration
Acceleration doubled = accel.times(2);                // 4 knots/sec

// State checks
boolean speeding = accel.isPositive();
boolean slowing = accel.isNegative();
boolean constant = accel.isZero();
```

---

## Course

Immutable representation of a directional angle (heading or bearing).

### Supported Units

| Unit | Range | Description |
|------|-------|-------------|
| Degrees | 0-360 | Standard compass |
| Radians | 0-2π | Mathematical |

### Creating Courses

```java
// From degrees
Course c1 = Course.ofDegrees(90);          // East
Course c2 = Course.ofDegrees(270);         // West
Course c3 = Course.ofDegrees(45);          // Northeast

// From radians
Course c4 = Course.ofRadians(Math.PI);     // South (180°)

// Using constants
Course north = Course.NORTH;               // 0°
Course east = Course.EAST;                 // 90°
Course south = Course.SOUTH;               // 180°
Course west = Course.WEST;                 // 270°
```

### Unit Conversion

```java
Course c = Course.ofDegrees(90);

double degrees = c.inDegrees();            // 90.0
double radians = c.inRadians();            // 1.5708 (π/2)
```

### Trigonometric Operations

```java
Course c = Course.ofDegrees(45);

double sine = c.sin();                     // 0.707...
double cosine = c.cos();                   // 0.707...
double tangent = c.tan();                  // 1.0
```

### Angular Arithmetic

```java
Course c1 = Course.ofDegrees(350);
Course c2 = Course.ofDegrees(20);

// Addition (handles wraparound)
Course sum = c1.plus(c2);                  // 10° (wraps around)

// Subtraction
Course diff = c1.minus(c2);                // 330°

// Scalar multiplication
Course doubled = c1.times(2);

// Angle between two courses (always positive, 0-180)
Course angle = Course.angleBetween(c1, c2);  // 30°
```

### Course Normalization

The `Course` class automatically normalizes angles to the 0-360 range:

```java
Course c1 = Course.ofDegrees(400);         // Stored as 40°
Course c2 = Course.ofDegrees(-90);         // Stored as 270°
```

---

## Common Patterns

### Unit-Safe Calculations

```java
// All arithmetic handles unit conversion automatically
Distance d1 = Distance.ofMeters(1000);
Distance d2 = Distance.ofFeet(3280);       // ~1000 meters

// This works correctly despite different units
Distance total = d1.plus(d2);              // ~2000 meters
boolean equal = d1.isLessThan(d2.times(1.01));  // true
```

### Chained Operations

```java
// Calculate ETA
Speed cruise = Speed.ofKnots(450);
Distance remaining = Distance.ofNauticalMiles(1500);

Duration flightTime = cruise.timeToTravel(remaining);
Distance fuelBurn = Speed.ofKnots(2500)    // fuel flow in lbs/hr
    .times(flightTime);                     // total fuel needed
```

### Working with Collections

```java
List<Distance> distances = List.of(
    Distance.ofMeters(100),
    Distance.ofMeters(200),
    Distance.ofMeters(300)
);

// Using streams
Distance total = distances.stream()
    .reduce(Distance.ofMeters(0), Distance::plus);

// Using static methods
Distance sum = Distance.sum(distances.toArray(Distance[]::new));
Distance avg = Distance.mean(distances.toArray(Distance[]::new));
```

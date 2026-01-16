# Temporal Classes

Classes for working with time instants, durations, and time windows.

## Time Utilities

Static utility class providing convenience methods for time operations.

### Duration Conversions

```java
Duration d = Duration.ofMinutes(90);

// Convert to decimal units
double hours = Time.getDecimalDuration(d, TimeUnit.HOURS);     // 1.5
double minutes = Time.getDecimalDuration(d, TimeUnit.MINUTES); // 90.0
double seconds = Time.getDecimalDuration(d, TimeUnit.SECONDS); // 5400.0
```

### Time Averaging

```java
Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
Instant t2 = Instant.parse("2024-01-01T12:00:00Z");

// Find the midpoint between two times
Instant midpoint = Time.averageTime(t1, t2);
// Result: 2024-01-01T11:00:00Z
```

### Duration Between Times

```java
Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
Instant t2 = Instant.parse("2024-01-01T12:00:00Z");
Instant t3 = Instant.parse("2024-01-01T14:00:00Z");

// Duration between earliest and latest
Duration span = Time.durationBtw(t1, t2, t3);  // 4 hours
```

### Finding Earliest/Latest

```java
Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
Instant t2 = Instant.parse("2024-01-01T12:00:00Z");
Instant t3 = Instant.parse("2024-01-01T11:00:00Z");

Instant first = Time.earliest(t1, t2, t3);   // t1 (10:00)
Instant last = Time.latest(t1, t2, t3);      // t2 (12:00)
```

### Time Ordering Validation

```java
Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
Instant t2 = Instant.parse("2024-01-01T11:00:00Z");
Instant t3 = Instant.parse("2024-01-01T12:00:00Z");

// Validate strict chronological order
Time.confirmStrictTimeOrdering(t1, t2, t3);  // OK

// Validate with tolerance (allow small out-of-order)
Duration tolerance = Duration.ofSeconds(5);
Time.confirmApproximateTimeOrdering(tolerance, t1, t2, t3);

// Validate array is sorted
Instant[] times = {t1, t2, t3};
Time.confirmTimeOrdering(times);  // Throws if not sorted
```

### Time Window Spanning

```java
Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
Instant t2 = Instant.parse("2024-01-01T14:00:00Z");
Instant t3 = Instant.parse("2024-01-01T12:00:00Z");

// Get TimeWindow that contains all times
TimeWindow window = Time.enclosingTimeWindow(t1, t2, t3);
// Result: window from 10:00 to 14:00
```

### String Formatting

```java
Instant t = Instant.parse("2024-01-01T10:30:45.123Z");

// Format as HH:mm:ss.SSS
String formatted = Time.asZTimeString(t);  // "10:30:45.123"
```

### Comparator for HasTime

```java
List<Position> positions = getPositions();

// Sort by time
positions.sort(Time.compareByTime());
```

### Literate Duration Comparisons

```java
Duration actual = Duration.ofMinutes(5);
Duration threshold = Duration.ofMinutes(10);

// Readable comparison syntax
boolean isShort = Time.theDuration(actual).isLessThan(threshold);           // true
boolean isLong = Time.theDuration(actual).isGreaterThan(threshold);         // false
boolean aboutSame = Time.theDuration(actual).isLessThanOrEqualTo(threshold); // true
```

---

## TimeWindow

Immutable representation of a fixed time interval with a start and end.

### Creating TimeWindows

```java
Instant start = Instant.parse("2024-01-01T10:00:00Z");
Instant end = Instant.parse("2024-01-01T14:00:00Z");

// Create a time window
TimeWindow window = TimeWindow.of(start, end);
```

### Accessing Boundaries

```java
TimeWindow window = TimeWindow.of(start, end);

Instant windowStart = window.start();
Instant windowEnd = window.end();
Duration windowDuration = window.duration();
Duration windowLength = window.length();  // Same as duration()
```

### State Checks

```java
TimeWindow window = TimeWindow.of(start, end);

boolean empty = window.isEmpty();     // true if start == end
boolean hasTime = !window.isEmpty();
```

### Containment Checks

```java
TimeWindow window = TimeWindow.of(start, end);
Instant testTime = Instant.parse("2024-01-01T12:00:00Z");

// Check if instant is within window
boolean inside = window.contains(testTime);  // true if start <= testTime <= end
```

### Overlap Operations

```java
TimeWindow w1 = TimeWindow.of(t1, t2);  // 10:00 - 14:00
TimeWindow w2 = TimeWindow.of(t3, t4);  // 12:00 - 16:00

// Check for overlap
boolean overlaps = w1.overlapsWith(w2);     // true

// Get the overlap region
TimeWindow overlap = w1.getOverlapWith(w2); // 12:00 - 14:00
```

### Window Manipulation

```java
TimeWindow window = TimeWindow.of(start, end);

// Expand the window by a duration (both ends)
TimeWindow padded = window.pad(Duration.ofMinutes(30));
// Result: start - 30min to end + 30min

// Shift the window in time
TimeWindow shifted = window.shift(Duration.ofHours(1));
// Result: start + 1hr to end + 1hr
```

### Fractional Position

```java
TimeWindow window = TimeWindow.of(start, end);  // 10:00 - 14:00 (4 hours)

// Map an instant to a fraction [0, 1]
Instant middle = Instant.parse("2024-01-01T12:00:00Z");
double fraction = window.toFractionOfRange(middle);  // 0.5

// Get instant at a fraction
Instant quarter = window.instantWithin(0.25);  // 11:00
```

### Time Stepping

```java
TimeWindow window = TimeWindow.of(start, end);
Duration step = Duration.ofMinutes(15);

// Get iterator for stepping through time
Iterator<Instant> iter = window.iterator(step);
while (iter.hasNext()) {
    Instant t = iter.next();
    // Process each 15-minute interval
}

// Get as list
List<Instant> steps = window.steppedIteration(step);

// Use enhanced for loop
for (Instant t : window.steppedIteration(Duration.ofMinutes(5))) {
    processTimeSlice(t);
}
```

---

## HasTime Interface

Common interface implemented by all time-aware classes.

### Implementations

- `Position`
- `KineticPosition`

### Interface Methods

```java
public interface HasTime {
    Instant time();

    default long timeAsEpochMs() {
        return time().toEpochMilli();
    }

    default Duration durationBtw(Instant otherTime) { ... }
    default Duration durationBtw(HasTime other) { ... }
}
```

### Static Utilities

```java
// Validate time is not null
HasTime.validate(position);  // Throws if position.time() is null

// Binary search in sorted list
List<Position> sortedPositions = getSortedPositions();
Instant targetTime = Instant.now();

int index = HasTime.binarySearch(sortedPositions, targetTime);

// Find floor (latest item at or before target)
Position floor = HasTime.floor(sortedPositions, targetTime);

// Find ceiling (earliest item at or after target)
Position ceiling = HasTime.ceiling(sortedPositions, targetTime);

// Find closest item to target time
Position closest = HasTime.closest(sortedPositions, targetTime);
```

---

## Common Patterns

### Processing Time Series Data

```java
List<Position> positions = getPositions();  // Sorted by time

// Validate temporal ordering
Time.confirmTimeOrdering(positions.stream()
    .map(Position::time)
    .toArray(Instant[]::new));

// Find time span
TimeWindow span = Time.enclosingTimeWindow(
    positions.stream()
        .map(Position::time)
        .toArray(Instant[]::new)
);

// Process in fixed intervals
for (Instant t : span.steppedIteration(Duration.ofSeconds(1))) {
    Position nearest = HasTime.closest(positions, t);
    process(t, nearest);
}
```

### Interpolation by Time

```java
List<Position> positions = getSortedPositions();
Instant targetTime = Instant.now();

// Find surrounding positions
Position before = HasTime.floor(positions, targetTime);
Position after = HasTime.ceiling(positions, targetTime);

if (before != null && after != null) {
    TimeWindow segment = TimeWindow.of(before.time(), after.time());
    double fraction = segment.toFractionOfRange(targetTime);
    // Interpolate between before and after using fraction
}
```

### Sliding Time Window

```java
Instant now = Instant.now();
Duration windowSize = Duration.ofMinutes(5);

TimeWindow slidingWindow = TimeWindow.of(
    now.minus(windowSize),
    now
);

List<Position> recentPositions = allPositions.stream()
    .filter(p -> slidingWindow.contains(p.time()))
    .toList();
```

### Rate Limiting by Time

```java
TimeWindow ratePeriod = TimeWindow.of(
    Instant.now().minus(Duration.ofMinutes(1)),
    Instant.now()
);

long requestCount = requests.stream()
    .filter(r -> ratePeriod.contains(r.time()))
    .count();

if (requestCount > MAX_REQUESTS_PER_MINUTE) {
    throw new RateLimitException();
}
```

### Aggregating by Time Buckets

```java
TimeWindow analysisWindow = TimeWindow.of(dayStart, dayEnd);
Duration bucketSize = Duration.ofHours(1);

Map<Instant, List<Position>> buckets = new HashMap<>();

for (Instant bucketStart : analysisWindow.steppedIteration(bucketSize)) {
    TimeWindow bucket = TimeWindow.of(bucketStart, bucketStart.plus(bucketSize));

    List<Position> inBucket = positions.stream()
        .filter(p -> bucket.contains(p.time()))
        .toList();

    buckets.put(bucketStart, inBucket);
}
```

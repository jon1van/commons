# TimeId

128-bit timestamp-based unique identifier with stateless generation.

## Structure

```
[42 bits: timestamp][86 bits: random]
|<--- 7 Base64 --->||<-- 15 Base64 -->|
```

- **Timestamp**: Milliseconds since epoch (enough for thousands of years)
- **Random**: SecureRandom bytes for uniqueness

## Creating TimeIds

### Current Time

```java
// Most common usage
TimeId id = TimeId.newId();
```

### Specific Time

```java
// For historical data or testing
Instant time = Instant.parse("2024-06-15T14:30:00Z");
TimeId id = TimeId.newIdFor(time);
```

### From Raw Bits

```java
// From two longs (advanced)
TimeId id = new TimeId(leftBits, rightBits);

// From byte array (16 bytes)
byte[] data = getIdBytes();
TimeId id = new TimeId(data);
```

## Serialization

### Base64 (Recommended)

Produces 22-character URL-safe string:

```java
TimeId id = TimeId.newId();

// Serialize
String encoded = id.asBase64();  // e.g., "YpnxRaaF_h64-ogTLrRE_g"

// Deserialize
TimeId restored = TimeId.fromBase64(encoded);

// toString() also returns Base64
String str = id.toString();
TimeId fromStr = TimeId.fromString(str);
```

### Hex String

Produces 32-character hex string:

```java
TimeId id = TimeId.newId();

// Serialize
String hex = id.asHexString();  // e.g., "609c2cf98dc9fa21d9633a14f800bbb6"

// Deserialize
TimeId restored = TimeId.fromHexString(hex);
```

### Raw Bytes

```java
TimeId id = TimeId.newId();

// Serialize (16 bytes)
byte[] bytes = id.bytes();

// Deserialize
TimeId restored = new TimeId(bytes);
```

## Extracting Timestamp

```java
TimeId id = TimeId.newId();

// As Instant
Instant created = id.time();

// As epoch milliseconds
long epochMs = id.timeAsEpochMs();

// Example
System.out.println("Created at: " + id.time());
// Output: Created at: 2024-06-15T14:30:00.123Z
```

## Sorting

TimeIds are naturally sortable by embedded timestamp:

```java
List<TimeId> ids = List.of(
    TimeId.newIdFor(Instant.parse("2024-06-15T12:00:00Z")),
    TimeId.newIdFor(Instant.parse("2024-06-15T10:00:00Z")),
    TimeId.newIdFor(Instant.parse("2024-06-15T11:00:00Z"))
);

Collections.sort(ids);
// Result: [10:00, 11:00, 12:00] chronologically
```

## Comparison and Equality

```java
TimeId id1 = TimeId.newId();
TimeId id2 = TimeId.newId();

// Comparison (by timestamp)
int cmp = id1.compareTo(id2);  // negative if id1 is older

// Equality (both timestamp and random bits)
boolean same = id1.equals(id2);  // false (different random bits)

// Hash code
int hash = id1.hashCode();
```

## Random Component

Access the random portion of the ID:

```java
TimeId id = TimeId.newId();

// Get random bits as bytes (16 bytes, first 5 zeroed)
byte[] randomBytes = id.randomBytes();

// Get last 15 Base64 characters (the random part)
String randomB64 = id.rngBitsAsBase64();

// Get as uniform random [0, 1)
double uniform = id.asUniformRand();
```

### Uniform Random for Sampling

The `asUniformRand()` method enables randomized sampling:

```java
// Sample 20 random items from a keyed dataset
Map<TimeId, Data> dataset = getDataset();

List<Data> sample = dataset.entrySet().stream()
    .sorted(Comparator.comparing(e -> e.getKey().asUniformRand()))
    .limit(20)
    .map(Map.Entry::getValue)
    .toList();
```

## Collision Probability

With 86 bits of randomness:

| IDs at Same Millisecond | Collision Probability |
|------------------------|----------------------|
| 1,000 | ~5.1e-22 |
| 1,000,000 | ~5.1e-16 |
| 100,000,000 | ~6.5e-11 |

Effectively zero for practical purposes.

## Common Patterns

### Database Primary Key

```java
public class Document {
    private final TimeId id;
    private String content;

    public Document(String content) {
        this.id = TimeId.newId();
        this.content = content;
    }

    public TimeId getId() { return id; }

    // Store as Base64 in database
    public String getIdString() { return id.asBase64(); }
}
```

### Event Sourcing

```java
public class Event {
    private final TimeId eventId;
    private final String type;
    private final Object payload;

    public Event(String type, Object payload) {
        this.eventId = TimeId.newId();
        this.type = type;
        this.payload = payload;
    }

    // Events naturally sort by occurrence time
    public Instant occurredAt() {
        return eventId.time();
    }
}

// Process events in order
List<Event> events = getEvents();
events.sort(Comparator.comparing(Event::eventId));
```

### File Naming

```java
// Generate unique, sortable filename
String filename = "report_" + TimeId.newId().asBase64() + ".pdf";

// Files will sort chronologically
// report_YpnxRaaF_h64-ogTLrRE_g.pdf
// report_YpnxRbbG_i75-phUMsTE_h.pdf
```

### Idempotency Keys

```java
public class ApiRequest {
    private final TimeId idempotencyKey;

    public ApiRequest() {
        this.idempotencyKey = TimeId.newId();
    }

    // Use for deduplication
    public String getIdempotencyKey() {
        return idempotencyKey.asBase64();
    }
}
```

## Thread Safety

TimeId generation is thread-safe. Multiple threads can call `TimeId.newId()` concurrently without synchronization.

## Best Practices

1. **Use Base64 for URLs and APIs** - URL-safe, compact
2. **Use bytes for binary protocols** - Most efficient
3. **Don't parse timestamps from string** - Use `time()` method
4. **Store as string in databases** - Unless using binary column
5. **Index by TimeId for chronological queries** - Natural ordering

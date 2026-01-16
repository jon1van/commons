# Commons IDs - Getting Started

Collision-proof, time-sortable unique identifiers for Java applications.

## Overview

Commons IDs provides two ID types:

| Type | Size | Use Case |
|------|------|----------|
| **TimeId** | 128-bit | Maximum collision resistance, stateless |
| **SmallTimeId** | 63-bit | Space-efficient, fits in `long` |

Both embed a timestamp for natural chronological ordering.

## Key Features

- **Collision-proof** - Statistically impossible to collide
- **Time-sortable** - Natural ordering by creation time
- **Timestamp extraction** - Recover creation time from ID
- **Compact serialization** - Base64 and hex encodings
- **Distributed-friendly** - No coordination required

## Quick Examples

### TimeId (128-bit)

```java
// Create new ID
TimeId id = TimeId.newId();

// Create for specific time
TimeId historical = TimeId.newIdFor(Instant.parse("2024-01-15T10:30:00Z"));

// Serialize
String base64 = id.asBase64();        // 22 characters
String hex = id.asHexString();        // 32 characters
byte[] bytes = id.bytes();            // 16 bytes

// Deserialize
TimeId restored = TimeId.fromBase64(base64);
TimeId fromHex = TimeId.fromHexString(hex);

// Extract timestamp
Instant created = id.time();
long epochMs = id.timeAsEpochMs();

// Sort by time
List<TimeId> ids = getIds();
Collections.sort(ids);  // Chronological order
```

### SmallTimeId (63-bit)

```java
// Create using factory (recommended)
IdFactoryShard factory = new IdFactoryShard(0, 1);  // Single factory
SmallTimeId id = factory.generateIdFor(Instant.now());

// Serialize
String hex = id.toString();           // 16 characters
long bits = id.id();                  // Raw long value

// Deserialize
SmallTimeId restored = SmallTimeId.fromString(hex);

// Extract timestamp
Instant created = id.time();
```

## When to Use What

### Use TimeId When:

- Full 128-bit storage is acceptable
- Stateless generation preferred
- Maximum collision resistance needed
- Database primary keys, document IDs

### Use SmallTimeId When:

- 64-bit constraint (fits in `long`)
- Memory/storage efficiency matters
- Distributed ID generation with coordination
- Database sharding scenarios

## ID Structure

### TimeId (128-bit)

```
[42 bits: timestamp] [86 bits: random]
```

- Timestamp: Milliseconds since epoch
- Random: SecureRandom for uniqueness

### SmallTimeId (63-bit)

```
[42 bits: timestamp] [21 bits: distinguishing]
```

- Timestamp: Milliseconds since epoch
- Distinguishing: Counter, hash, or shard-based

## Comparison with Other IDs

| Feature | TimeId | SmallTimeId | UUID v4 | UUID v7 |
|---------|--------|-------------|---------|---------|
| Size | 128-bit | 63-bit | 128-bit | 128-bit |
| Sortable | Yes | Yes | No | Yes |
| Extractable time | Yes | Yes | No | Yes |
| Stateless | Yes | No* | Yes | Yes |
| Collision risk | ~0 | ~0** | Very low | Very low |

*Can be stateless with hashing but higher collision risk
**When using proper factories

## Next Steps

- [TimeId](02-time-id.md) - 128-bit stateless IDs
- [SmallTimeId](03-small-time-id.md) - 63-bit efficient IDs
- [Distributed Generation](04-distributed.md) - IdFactoryShard for distributed systems

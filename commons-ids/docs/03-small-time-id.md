# SmallTimeId

63-bit timestamp-based unique identifier that fits in a Java `long`.

## Structure

```
[42 bits: timestamp][21 bits: distinguishing]
```

- **Timestamp**: Milliseconds since epoch
- **Distinguishing**: Application-provided bits for uniqueness

## Creating SmallTimeIds

Unlike TimeId, SmallTimeId requires you to provide the 21 distinguishing bits. This is typically done via a factory.

### Using IdFactoryShard (Recommended)

```java
// Single-server scenario
IdFactoryShard factory = new IdFactoryShard(0, 1);

SmallTimeId id = factory.generateIdFor(Instant.now());
```

### Using TimeIds Factory Methods

```java
// Solo factory (single generator)
TimeIds.IdFactory<SmallTimeId> factory = TimeIds.soloFactory(
    IdFactoryShard.inMemoryCounter()
);

SmallTimeId id = factory.generateIdFor(Instant.now());
```

### Direct Construction (Advanced)

```java
// You provide the 21 bits
long twentyOneBits = computeDistinguishingBits();
SmallTimeId id = new SmallTimeId(Instant.now(), twentyOneBits);

// Or via utility
SmallTimeId id = TimeIds.directBitsetTimeId(Instant.now(), twentyOneBits);
```

## Serialization

### Hex String

```java
SmallTimeId id = factory.generateIdFor(Instant.now());

// Serialize (16 hex characters)
String hex = id.toString();  // e.g., "609c2cf98dc9fa21"

// Deserialize
SmallTimeId restored = SmallTimeId.fromString(hex);
```

### Raw Long

```java
SmallTimeId id = factory.generateIdFor(Instant.now());

// Get raw bits
long bits = id.id();

// Reconstruct (need factory or known distinguishing bits)
```

## Extracting Timestamp

```java
SmallTimeId id = factory.generateIdFor(Instant.now());

// As Instant
Instant created = id.time();

// As epoch milliseconds
long epochMs = id.timeAsEpochMs();
```

## Extracting Distinguishing Bits

```java
SmallTimeId id = factory.generateIdFor(Instant.now());

// Get the 21 non-time bits
long distinguishing = id.nonTimeBits();
```

## Sorting

SmallTimeIds sort by timestamp:

```java
List<SmallTimeId> ids = generateIds();
Collections.sort(ids);  // Chronological order
```

## Comparison and Equality

```java
SmallTimeId id1 = factory.generateIdFor(Instant.now());
SmallTimeId id2 = factory.generateIdFor(Instant.now());

// Comparison (by full 63-bit value)
int cmp = id1.compareTo(id2);

// Equality
boolean same = id1.equals(id2);

// Hash code (uses lowest 32 bits to avoid birthday attacks)
int hash = id1.hashCode();
```

## 21-Bit Space

The 21 distinguishing bits allow for 2,097,152 unique values:

| Strategy | Uniqueness Guarantee |
|----------|---------------------|
| Counter-based (IdFactoryShard) | Perfect within shard |
| Hash-based | Birthday problem applies |
| Random | Birthday problem applies |

### Birthday Problem

With random/hash-based bits, collision probability:

| IDs at Same Millisecond | Collision Probability |
|------------------------|----------------------|
| 100 | ~0.2% |
| 1,000 | ~21% |
| 2,000 | ~61% |

**Recommendation**: Use `IdFactoryShard` for guaranteed uniqueness.

## Common Patterns

### Database Column

```java
// Fits in BIGINT column
public class Record {
    private final long id;  // Store SmallTimeId as long

    public Record() {
        this.id = factory.generateIdFor(Instant.now()).id();
    }

    public SmallTimeId getId() {
        // Reconstruct when needed
        return SmallTimeId.fromString(Long.toHexString(id));
    }
}
```

### Efficient Storage

```java
// SmallTimeId uses 8 bytes vs TimeId's 16 bytes
// For millions of IDs, this saves significant memory

long[] ids = new long[1_000_000];
for (int i = 0; i < ids.length; i++) {
    ids[i] = factory.generateIdFor(Instant.now()).id();
}
```

### Time-Based Partitioning

```java
SmallTimeId id = getRecord().getId();

// Extract time for partitioning
Instant created = id.time();
String partition = created.toString().substring(0, 10);  // "2024-06-15"
```

## Choosing Distinguishing Bit Strategy

### Counter (IdFactoryShard)

Best for guaranteed uniqueness:

```java
IdFactoryShard factory = new IdFactoryShard(0, 1);
// Uses incrementing counter per millisecond
// 2,097,152 IDs per millisecond guaranteed unique
```

### Hashing

Good for deterministic IDs from input:

```java
long bits = BitAndHashingUtils.nRandomBitsFrom(21, inputString);
SmallTimeId id = new SmallTimeId(Instant.now(), bits);
// Same input always produces same distinguishing bits
// Risk: Birthday problem for many IDs at same time
```

### Shard + Counter (Distributed)

Best for distributed systems:

```java
// Server 1
IdFactoryShard shard1 = new IdFactoryShard(0, 4);

// Server 2
IdFactoryShard shard2 = new IdFactoryShard(1, 4);

// Each shard has its own counter space
// No coordination needed
```

## Limitations

1. **Requires factory/strategy** - Cannot generate stateless like TimeId
2. **Smaller random space** - Only 21 bits vs TimeId's 86
3. **Birthday problem** - If using hash/random bits
4. **Counter exhaustion** - Max ~2M IDs per millisecond per shard

## When to Prefer TimeId

- Stateless generation required
- Higher collision resistance needed
- Not storage-constrained
- Simpler API preferred

## When to Prefer SmallTimeId

- Must fit in `long`/BIGINT
- Memory/storage optimization critical
- Can use IdFactoryShard properly
- Distributed ID generation with sharding

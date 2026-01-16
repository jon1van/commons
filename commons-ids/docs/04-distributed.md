# Distributed ID Generation

Using IdFactoryShard for collision-free distributed ID generation.

## Overview

IdFactoryShard implements the Snowflake ID pattern: multiple non-communicating factories can generate IDs without coordination by partitioning the bit space.

## Bit Allocation

The 21 distinguishing bits are split between:

```
[shard index bits][counter bits]
```

| Total Shards | Shard Bits | Counter Bits | IDs per ms per shard |
|--------------|------------|--------------|---------------------|
| 1 | 0 | 21 | 2,097,152 |
| 2 | 1 | 20 | 1,048,576 |
| 4 | 2 | 19 | 524,288 |
| 8 | 3 | 18 | 262,144 |
| 16 | 4 | 17 | 131,072 |
| 1024 | 10 | 11 | 2,048 |

## Creating an IdFactoryShard

### Single Factory

```java
// Shard index 0, total shards 1
IdFactoryShard factory = new IdFactoryShard(0, 1);

SmallTimeId id = factory.generateIdFor(Instant.now());
```

### Multiple Factories

```java
// Server 1: shard 0 of 4
IdFactoryShard factory1 = new IdFactoryShard(0, 4);

// Server 2: shard 1 of 4
IdFactoryShard factory2 = new IdFactoryShard(1, 4);

// Server 3: shard 2 of 4
IdFactoryShard factory3 = new IdFactoryShard(2, 4);

// Server 4: shard 3 of 4
IdFactoryShard factory4 = new IdFactoryShard(3, 4);

// All can generate IDs simultaneously without collision
```

### Enum-Based Sharding

```java
public enum Server {
    PRIMARY, REPLICA_1, REPLICA_2, REPLICA_3
}

// Automatically uses enum ordinal as shard index
IdFactoryShard factory = IdFactoryShard.newFactoryShardFor(Server.PRIMARY);
```

## CountKeeper Strategies

CountKeeper tracks the next counter value for each timestamp.

### In-Memory (Default)

```java
// Unbounded memory - counter never resets
IdFactoryShard factory = new IdFactoryShard(0, 1);

// Or explicitly
IdFactoryShard factory = new IdFactoryShard(
    0, 1,
    IdFactoryShard.inMemoryCounter()
);
```

**Pros**: Simple, fast
**Cons**: Memory grows over time if generating IDs across many timestamps

### Limited Memory

```java
// Keeps only last N timestamps
CountKeeper counter = IdFactoryShard.limitedMemoryCounter(1000);

IdFactoryShard factory = new IdFactoryShard(0, 1, counter);
```

**Pros**: Bounded memory
**Cons**: Counter resets if timestamp revisited after eviction

## Using TimeIds Factory Methods

Alternative API for creating factories:

```java
// Solo factory
TimeIds.IdFactory<SmallTimeId> solo = TimeIds.soloFactory(
    IdFactoryShard.inMemoryCounter()
);

// Team member factory
TimeIds.IdFactory<SmallTimeId> member = TimeIds.factoryTeamMember(
    0,      // shard index
    4,      // total shards
    IdFactoryShard.inMemoryCounter()
);

// Enum-based team member
TimeIds.IdFactory<SmallTimeId> enumMember = TimeIds.factoryEnumTeamMember(
    Server.PRIMARY,
    IdFactoryShard.inMemoryCounter()
);

// Generate ID
SmallTimeId id = solo.generateIdFor(Instant.now());
```

## Querying Factory Configuration

```java
IdFactoryShard factory = new IdFactoryShard(2, 8);

int shardIndex = factory.shardIndex();           // 2
int totalShards = factory.numShardsInTeam();     // 8
int shardBits = factory.numBitsToStoreShardIndex();  // 3
int counterBits = factory.numBitsForItemDistinction();  // 18
int maxPerMs = factory.limitTimeIdsPerEpochMills();  // 262,144
```

## Bit Calculation Utility

```java
// How many bits needed for N shards?
int bits = IdFactoryShard.numBitsRequiredFor(8);   // 3
int bits = IdFactoryShard.numBitsRequiredFor(100); // 7
int bits = IdFactoryShard.numBitsRequiredFor(1);   // 0
```

## Deployment Patterns

### Fixed Shard Assignment

Assign shard indices at deployment time:

```yaml
# server-1.yml
id_shard_index: 0
id_total_shards: 4

# server-2.yml
id_shard_index: 1
id_total_shards: 4
```

```java
int shardIndex = config.getInt("id_shard_index");
int totalShards = config.getInt("id_total_shards");

IdFactoryShard factory = new IdFactoryShard(shardIndex, totalShards);
```

### Dynamic Shard Assignment

Use service discovery to assign shards:

```java
// Get unique index from coordinator (Zookeeper, Consul, etc.)
int shardIndex = coordinator.acquireShardIndex("id-generator");
int totalShards = coordinator.getTotalShards("id-generator");

IdFactoryShard factory = new IdFactoryShard(shardIndex, totalShards);
```

### Database-Based Sharding

```java
// Each database shard gets its own ID factory
public class DatabaseShard {
    private final int shardId;
    private final IdFactoryShard idFactory;

    public DatabaseShard(int shardId, int totalShards) {
        this.shardId = shardId;
        this.idFactory = new IdFactoryShard(shardId, totalShards);
    }

    public SmallTimeId generateId() {
        return idFactory.generateIdFor(Instant.now());
    }
}
```

## Thread Safety

IdFactoryShard is **not thread-safe** by default. For concurrent access:

```java
// Option 1: Synchronized wrapper
IdFactoryShard delegate = new IdFactoryShard(0, 1);
Object lock = new Object();

public SmallTimeId generateId() {
    synchronized (lock) {
        return delegate.generateIdFor(Instant.now());
    }
}

// Option 2: Thread-local factories
ThreadLocal<IdFactoryShard> factoryLocal = ThreadLocal.withInitial(() ->
    new IdFactoryShard(0, 1)
);

public SmallTimeId generateId() {
    return factoryLocal.get().generateIdFor(Instant.now());
}

// Option 3: One factory per thread with unique shard
// (Requires knowing max thread count)
```

## Common Pitfalls

### Wrong Shard Count

```java
// BAD: Different totals cause overlapping bit spaces
IdFactoryShard server1 = new IdFactoryShard(0, 4);
IdFactoryShard server2 = new IdFactoryShard(1, 8);  // Wrong!

// GOOD: Same total across all factories
IdFactoryShard server1 = new IdFactoryShard(0, 4);
IdFactoryShard server2 = new IdFactoryShard(1, 4);
```

### Duplicate Shard Index

```java
// BAD: Same shard index = guaranteed collisions
IdFactoryShard server1 = new IdFactoryShard(0, 4);
IdFactoryShard server2 = new IdFactoryShard(0, 4);  // Collision!

// GOOD: Unique indices
IdFactoryShard server1 = new IdFactoryShard(0, 4);
IdFactoryShard server2 = new IdFactoryShard(1, 4);
```

### Counter Exhaustion

```java
IdFactoryShard factory = new IdFactoryShard(0, 1024);
// Only 2,048 IDs per millisecond!

// If generating more than limit, will throw or wrap
for (int i = 0; i < 10_000; i++) {
    factory.generateIdFor(fixedInstant);  // Problem!
}
```

## Scaling Considerations

### Adding Shards

Cannot simply add shards without reassigning:

```java
// Original: 4 shards
// Shard 0: bits 00xxx...
// Shard 1: bits 01xxx...
// Shard 2: bits 10xxx...
// Shard 3: bits 11xxx...

// Adding shard 4 would require 3 bits for shard index
// This changes bit allocation for ALL shards
```

**Solution**: Plan for growth by allocating more shards than initially needed.

### High Throughput

For very high throughput, use more shards:

```java
// 2M IDs/ms single shard
IdFactoryShard high = new IdFactoryShard(0, 1);

// If need more, use multiple shards per server
IdFactoryShard shard1 = new IdFactoryShard(0, 16);
IdFactoryShard shard2 = new IdFactoryShard(1, 16);
// Round-robin between them
```

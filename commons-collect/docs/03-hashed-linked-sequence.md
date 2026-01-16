# HashedLinkedSequence

A hybrid data structure combining HashSet's O(1) lookup with LinkedList's ordering.

## Overview

HashedLinkedSequence provides:

- O(1) membership checks (like HashSet)
- O(1) navigation to adjacent elements
- O(1) insertion before/after existing elements
- Maintained insertion order
- No duplicates allowed

## Creating a HashedLinkedSequence

```java
// Empty sequence
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence();

// From array
String[] data = {"a", "b", "c", "d"};
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence(data);

// From collection
List<String> list = List.of("a", "b", "c");
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence(list);
```

## Adding Elements

```java
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence();

// Add to ends
seq.addFirst("a");  // [a]
seq.addLast("b");   // [a, b]
seq.addLast("c");   // [a, b, c]

// Standard add (appends to end)
seq.add("d");       // [a, b, c, d]

// Add all from collection
seq.addAll(List.of("e", "f"));  // [a, b, c, d, e, f]
```

## Accessing Elements

```java
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence(
    new String[]{"a", "b", "c", "d", "e"}
);

// Access ends
String first = seq.getFirst();  // "a"
String last = seq.getLast();    // "e"

// Check membership
boolean hasC = seq.contains("c");  // true - O(1)

// Size
int size = seq.size();
boolean empty = seq.isEmpty();
```

## Navigation

Navigate relative to existing elements in O(1):

```java
// Sequence: [a, b, c, d, e]

String afterB = seq.getElementAfter("b");   // "c"
String beforeD = seq.getElementBefore("d"); // "c"

// At boundaries
String afterE = seq.getElementAfter("e");   // null (no next)
String beforeA = seq.getElementBefore("a"); // null (no previous)
```

## Positional Insertion

Insert relative to existing elements in O(1):

```java
// Start: [a, b, c]

seq.insertAfter("x", "a");   // [a, x, b, c]
seq.insertBefore("y", "c");  // [a, x, b, y, c]
seq.insertAfter("z", "c");   // [a, x, b, y, c, z]
```

## Removing Elements

```java
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence(
    new String[]{"a", "b", "c", "d"}
);

// Remove by value
seq.remove("b");  // [a, c, d]

// Remove from ends
seq.removeFirst();  // [c, d]
seq.removeLast();   // [c]

// Remove all
seq.removeAll(List.of("x", "c"));  // []

// Clear
seq.clear();
```

## Iteration

```java
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence(
    new String[]{"a", "b", "c"}
);

// For-each loop (maintains insertion order)
for (String item : seq) {
    System.out.println(item);
}

// Iterator
Iterator<String> iter = seq.iterator();
while (iter.hasNext()) {
    String item = iter.next();
    if (shouldRemove(item)) {
        iter.remove();
    }
}

// Stream
seq.stream()
    .filter(s -> s.length() > 1)
    .forEach(System.out::println);
```

## Conversion

```java
// To array
Object[] arr = seq.toArray();
String[] strArr = seq.toArray(new String[0]);

// To other collections
List<String> list = new ArrayList<>(seq);
Set<String> set = new HashSet<>(seq);
```

## Comparison with Other Collections

| Operation | HashedLinkedSequence | LinkedList | HashSet |
|-----------|---------------------|------------|---------|
| contains | O(1) | O(n) | O(1) |
| add (end) | O(1) | O(1) | O(1) |
| remove | O(1) | O(n) | O(1) |
| getFirst/Last | O(1) | O(1) | N/A |
| getAfter/Before | O(1) | O(n)* | N/A |
| insertAfter/Before | O(1) | O(n)* | N/A |
| Maintains order | Yes | Yes | No |
| Allows duplicates | No | Yes | No |

*LinkedList requires finding the element first

## Common Patterns

### Ordered Processing Queue

```java
HashedLinkedSequence<Task> queue = HashedLinkedSequence.newHashedLinkedSequence();

// Add tasks
queue.addLast(task1);
queue.addLast(task2);

// Process in order
while (!queue.isEmpty()) {
    Task task = queue.getFirst();
    process(task);
    queue.removeFirst();
}
```

### Priority Insertion

```java
HashedLinkedSequence<Job> jobs = HashedLinkedSequence.newHashedLinkedSequence();

// Add initial jobs
jobs.addLast(job1);
jobs.addLast(job2);
jobs.addLast(job3);

// Insert high-priority job after specific job
jobs.insertAfter(urgentJob, job1);
```

### Deduplication with Order

```java
// Maintain insertion order while removing duplicates
List<String> withDupes = List.of("a", "b", "a", "c", "b", "d");
HashedLinkedSequence<String> unique = HashedLinkedSequence.newHashedLinkedSequence();

for (String s : withDupes) {
    unique.add(s);  // Duplicates silently ignored
}
// Result: [a, b, c, d]
```

### Circular Navigation

```java
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence(
    new String[]{"a", "b", "c", "d"}
);

String current = seq.getFirst();
while (true) {
    process(current);

    String next = seq.getElementAfter(current);
    if (next == null) {
        next = seq.getFirst();  // Wrap around
    }
    current = next;

    if (shouldStop()) break;
}
```

### Sliding Window

```java
HashedLinkedSequence<Event> window = HashedLinkedSequence.newHashedLinkedSequence();

void addEvent(Event e) {
    window.addLast(e);

    // Remove old events
    while (window.size() > MAX_WINDOW_SIZE) {
        window.removeFirst();
    }
}

boolean recentlyOccurred(Event e) {
    return window.contains(e);  // O(1)
}
```

## Thread Safety

HashedLinkedSequence is **not thread-safe**. For concurrent access:

```java
// External synchronization
HashedLinkedSequence<String> seq = HashedLinkedSequence.newHashedLinkedSequence();
Object lock = new Object();

synchronized (lock) {
    seq.add("item");
}

// Or wrap in synchronized collection
Collection<String> syncSeq = Collections.synchronizedCollection(seq);
```

## Limitations

1. **No index-based access** - Cannot do `get(int index)`
2. **No duplicates** - Each element can only appear once
3. **Requires proper equals/hashCode** - Elements must implement these correctly
4. **Not a List** - Implements Collection and Set, not List

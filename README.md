# Commons

This project contains multiple library components for use elsewhere.

## The Modules

- **[Units](#units)**
- **[Maps](#maps)**
- **[Id](#id)**
- **[Collect](#collect)**
- **[Utils](#utils)**

## Units

The units package simplify dealing with time-stamped location data. The core
classes [LatLong](./commons-units/src/main/java/io/github/jon1van/units/LatLong.java), [Distance](./commons-units/src/main/java/io/github/jon1van/units/Distance.java), [Speed](./commons-units/src/main/java/io/github/jon1van/units/Speed.java), [Course](./commons-units/src/main/java/io/github/jon1van/units/Course.java),
[TimeWindow](./commons-units/src/main/java/io/github/jon1van/units/TimeWindow.java) and others provide a literate API
for common tasks and elimination common mistakes (e.g., accounting for curvature of the earth, and making sure distance
unit conversions are always correct).

The _spherical_ math backing class the distance computations in this package is
in [Navigation](./commons-units/src/main/java/io/github/jon1van/units/Navigation.java). Beware, computing distance on a
spheroid is not straightforward.

These units classes are key pieces of the public API of the Maps package

### Code examples

```
// Simple location, distance, speed, and direction tooling
LatLong nyc = LatLong.of(40.7859, -73.9624);
LatLong la = LatLong.of(3.9427, -118.4100);
Distance dist = nyc.distanceTo(la);
Speed spd = dist.dividedBy(Duration.ofHours(4));
Course direction = nyc.courseTo(la);


// Time domain tooling
Instant[] times = getTimestamps();

TimeWindow window = Time.enclosingTimeWindow(times);
Instant startTime = window.start();
Instant endTime = window.end();
Duration timeSpan = window.length();
Iterator<Instant> every2sec = window.iterator(Duration.ofSeconds(2L));
```

## Maps

The maps package allows drawing custom maps on top of MapBox Tiles. This package is tightly integrated with the units
package. This package's main public entry points
are: [MapBuilder](./commons-maps/src/main/java/io/github/jon1van/maps/MapBuilder.java),
[MapFeatures](./commons-maps/src/main/java/io/github/jon1van/maps/MapFeatures.java), and
[MapImage](./commons-maps/src/main/java/io/github/jon1van/maps/MapImage.java).

```
// Plot a map that shows all the LatLong points within 2 miles of the centerpoint
LatLong centerPoint = LatLong.of(12.3, 4.567);

Collection<LatLong> locationData = allLatLongs();
Distance radius = Distance.ofMiles(2);
List<LatLong> closeBy = locationData.filter(x -> x.isWithin(radius, centerPoint);
List<LatLong> farAway = locationData.filter(x -> !x.isWithin(radius, centerPoint);

MapBuilder.newMapBuilder()
    .tileSource(new MonochromeTileServer(Color.BLACK))
    .center(centerPoint)
    .width(3200, 6)
    .addFeatures(closeBy, a -> MapFeatures.filledCircle(a.latLong(), Color.RED, 10))
    .addFeatures(farAway, b -> MapFeatures.filledCircle(b.latLong(), Color.CYAN, 10))
    .addFeatures(MapFeatures.filledCircle(centerPoint, Color.MAGENTA, 14))
    .toFile(new File("nearByPoints.png"));
```

Additional Map making documentation is [here](./docs/mapping.md)

## ID

The tiny `id` package is for importing [TimeId](./commons-id/src/main/java/io/github/jon1van/ids/TimeId.java)
and [SmallTimeId](./commons-id/src/main/java/io/github/jon1van/ids/SmallTimeId.java)

These ID classes provide collision-proof "UUID-like" behavior while also encoding an epoch millisecond
timestamp. [TimeId](./commons-id/src/main/java/io/github/jon1van/ids/TimeId.java) uses 128bits and is safe to use
anywhere.  `TimeId` is similar to UUID v7 and Snowflake ID
[SmallTimeId](./commons-id/src/main/java/io/github/jon1van/ids/SmallTimeId.java) uses half the size (64 bits) and
requires meeting certain constraints. Learn more [here](./docs/timeIdDesign.md)

## Collect

The `collect` package contains custom data
structures: [MetricTree](./commons-collect/src/main/java/io/github/jon1van/collect/MetricTree.java)
, [MetricSet](./commons-collect/src/main/java/io/github/jon1van/collect/MetricSet.java)
, and  [HashedLinkedSequence](./commons-collect/src/main/java/io/github/jon1van/collect/HashedLinkedSequence.java)

`MetricTree` and `MetricSet` provide efficient k-nearest neighbor search in multidimensional space. These
data-structures are often configured with the classes from [Units](#units)

```
// Start with this data
Map<LatLong, String> businessLocations = getBusinessData();
var myLocation = LatLong.of(12.345,  67.890);

// Organize this Key/Value data by "distance between keys"
DistanceMetric<LatLong> metric = (a, b) -> a.distanceTo(b).inMeters();
MetricTree<LatLong, String> tree = new MetricTree<>(metric);
tree.putAll(businessLocations);

// Simply find the 5 closest businesses to your location .. 
List<SearchResult<LatLong, String>> results = tree.getNClosest(myLocation, 5);
```

The `HashedLinkedSequence` is a data structure that combines aspects of HashSet and LinkedList so that inserts and
searches are all constant time operations.

```
String[] data = {"a", "b", "c", "d", "e", "f", "g", "h"};

HashedLinkedSequence<String> hashedSequence = newHashedLinkedSequence(data);

hashedSequence.insertBefore("x", "e");
hashedSequence.insertAfter("y", "e");

// e.g. {a, b, c, d, x, e, y, f, g, h}
hashedSequence.getElementAfter("x");  // return "e"
hashedSequence.getElementBefore("y");  // returns "e"
```

## Utils

The `utils` package contains two important sub packages and a handful of miscellaneous utility classes that didn't have
a better home.

The two important subpackages are `uncheck` and `math`.

### Exception Handling and Streams

The `uncheck` package contains classes that drastically reduce the need for useless `try/catch` bloat everywhere
CheckedExceptions are thrown and
rethrown. [DemotedException](./commons-utils/src/main/java/io/github/jon1van/uncheck/DemotedException.java) converts any
CheckedException into a runtime exception. Using `DemotedException` inside your `catch` blocks means your methods don't
need to add `throws` clauses to their signatures.

```
// before:  Throws a IOException that callers MUST handle 
public void doWork(File f) throws IOException { ... }

// after:  Throws a RuntimeException in place of the IOException.  The ioe is still accessible in an upstream catch clause
public void doWork(File f) {
    try { ...}
    catch (IOException ex) {
        throw DemotedException.demote(ex);
    }
}
```

The `uncheck` package also helps simplify using standard java Stream and FunctionalInterfaces (e.g. `Function` and
`Predicate`)

```
// before:  This stream is hard to read and hard to write because of checked exceptions!
List<String> subset = myDataSet.stream()
    .map(str -> {
        try {
            return functionThatThrowsCheckedEx(str);
        } catch (AnnoyingCheckedException ex) {
            throw new RuntimeException(ex);
        }})
    .filter(str -> str.length() < 5)
    .toList();

// after:  This stream is easier to read and write due to `uncheck`
List<String> subset = myDataSet.stream()
    .map(Uncheck.func(str -> functionThatThrowsCheckedEx(str))
    .filter(str -> str.length() < 5)
    .toList();
```

### Curve Fitting Position Data

Inside `math` you will
find [PositionInterpolator](./commons-utils/src/main/java/io/github/jon1van/math/locationfit/PositionInterpolator.java)
and its
implementation [LocalPolyInterpolator](./commons-utils/src/main/java/io/github/jon1van/math/locationfit/LocalPolyInterpolator.java).
This interpolator uses polynomial curve fitting to convert raw a sequence of
time-stamped [Position](./commons-units/src/main/java/io/github/jon1van/units/Position.java) measurements to
enriched [KineticPosition](./commons-units/src/main/java/io/github/jon1van/units/KineticPosition.java) records. The
resulting `KineticPosition` records provide numerically sound estimates of Location, Speed, Direction, and Acceleration.

### Miscellaneous Utilities

Other utilities include:

- [NeighborIterator](./commons-utils/src/main/java/io/github/jon1van/utils/NeighborIterator.java) for iterating pairs of
  consecutive items in an iteration. For example, the list {1, 2, 3, 4, 5} would yield the iteration {1, 2}, {2, 3}, {3,
  4}, {4, 5}.
- [SingleUseTimer](./commons-utils/src/main/java/io/github/jon1van/utils/SingleUseTimer.java) for timing individual
  `Runnable`s (Similar to Guava's StopWatch)
- [ErrorCatchingTask](./commons-utils/src/main/java/io/github/jon1van/utils/ErrorCatchingTask.java)
  and [ExceptionHandler](./commons-utils/src/main/java/io/github/jon1van/utils/ExceptionHandler.java) make using
  `Runnables` in `ExecutorServices` more robust.
- [FileLineIterator](./commons-utils/src/main/java/io/github/jon1van/utils/FileLineIterator.java) for iterating the
  lines inside a text file. This utility is better that other similar tools because it handles `.gz` files.
- [ConsumingCollections](./commons-utils/src/main/java/io/github/jon1van/func/ConsumingCollections.java) decorates
  standard Java Collections with the `Consumer` interface. This is useful when testing infinite streaming applications
  that emit data via a `Consumer`. The `func` package contains other utilities for streaming data processing.
- [PropertyUtils](./commons-utils/src/main/java/io/github/jon1van/utils/PropertyUtils.java) for java.util.Properties
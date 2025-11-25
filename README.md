# Commons

This project contains multiple library components for use elsewhere.

## The Modules

- **[Units](#units)**
- **[Maps](#maps)**
- **[Id](#id)**
- **Collect**
- **Utils**

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
requires meeting certain constraints.  Learn more [here](./docs/timeIdDesign.md)

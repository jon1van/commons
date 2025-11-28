## The Map Making Features in Commons

The package `io.github.jon1van.maps` contains utilities for map making.

1. Using `MapBuilder` is the easiest way to make maps. The features in this package are designed for this Builder.
2. You can also create a maps using `MapImage` directly. This class combine map tiles from a `TileServer` into a single
   properly cropped image.
3. `MapFeatures` and `FeatureSetBuilder` provide fluent apis for creating objects to be drawn on a map.
4. The `PixelLatLong` class translates between "real world" `LatLong` locations and the pixel coordinates system used in
   map tiles.

### TileServers

A `TileServer` is a service that
provides [map tiles](https://docs.microsoft.com/en-us/azure/azure-maps/zoom-levels-and-tile-grid?tabs=csharp). 

This package has a few built in TileServer implementation:
- **`MapBoxAPI`**: provides tiles loaded from [MapBox](https://www.mapbox.com/)
- **`MonochromeTileServer`:** provides tiles with a single background color.
- **`DebugTileServer`:** provides tiles with a "test pattern" to help debug map making issues.

### MapBox API Token

Using the MapBox API requires setting the `MAPBOX_ACCESS_TOKEN` variable. This can be three ways:
1. Setting a environment variable `MAPBOX_ACCESS_TOKEN` to a valid mapbox api token, 
2. setting a Java System property `MAPBOX_ACCESS_TOKEN` to a valid mapbox api token, 
3. or placing a file named `mapbox.token` in the user directory. This file must have a single line 
   - `MAPBOX_ACCESS_TOKEN=this.isNotAValidJwtToken.EvenThoughILookLikeOne`

### Making an Undecorated Map

Create a MapImage directly using the constructor.

```
MapImage map = new MapImage(
    new MapBoxApi(DARK), 
    LatLong.of(32.8968, -97.0380), 
    Distance.ofNauticalMiles(10)
);
```

Once you have the `MapImage` you can:

1. Export it as a BufferedImage using: `BufferedImage img = map.plot();`
2. Write it to a File using: `map.plotToFile(new File("mapWithoutDecoration.jpg"));`

![regular map](./mapWithoutDecoration.jpg)

### Adding MapFeatures to a Map

Here is an example of drawing stuff on a map

```
LatLong lostDog = LatLong.of(38.9223, -77.2016);

MapImage map = new MapImage(
    new MapBoxApi(DARK),
    lostDog,
    Distance.ofNauticalMiles(2.5)
);

//create a list of MapFeatures that need to be drawn...
FeatureSet features = newFeatureSetBuilder()
    .addCircle(lostDog, Color.RED, 30, 4.0f)
    .addLine(
        lostDog.project(NORTH, Distance.ofNauticalMiles(1.0)),
        lostDog.project(SOUTH, Distance.ofNauticalMiles(1.0)),
        Color.MAGENTA,
        1.f
    )
    .addFilledShape(
        boxAround(lostDog),
        new Color(255, 255, 255, 25)) //use Alpha channel for transparency!
     .build();

map.plotToFile(features, new File("mapWithDecoration.jpg"));
```

![regular map](./mapWithDecoration.jpg)

### Using MapBuilder to build maps

The `MapImage` and `FeatureSet` classes grows less convenient to use as the maps you make get more complicated.
Using `MapBuilder` will help ease the burden. Here are some examples

example 1

```
newMapBuilder()
    .mapBoxDarkMode()
    .center(LatLong.of(32.8968, -97.0380))
    .width(Distance.ofNauticalMiles(10))
    .addFeature(randomPath())
    .toFile(new File("random walk.png"));
```

example 2

```
newMapBuilder()
    .mapBoxDarkMode()
    .center(home)
    .width(Distance.ofNauticalMiles(7.5))
    .useLocalDiskCaching(Duration.ofDays(7))
    .addFeatures(circlesForRawRadarHits)
    .addFeature(smoothedTrackPath)
    .toFile(new File("trackWithGentleError.png"));
```

![regular map](./trackWithGentleError.png)


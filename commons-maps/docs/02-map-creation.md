# Map Creation

Creating maps using MapBuilder and MapImage.

## MapBuilder

Fluent builder API for configuring and rendering maps.

### Basic Usage

```java
BufferedImage map = MapBuilder.newMapBuilder()
    .mapBoxDarkMode()                    // Tile source
    .center(LatLong.of(40.7128, -74.0060))
    .width(Distance.ofMiles(5))          // Real-world width
    .toImage();
```

### Setting the Tile Source

```java
// MapBox styles (requires API token)
.mapBoxDarkMode()
.mapBoxLightMode()
.mapBoxSatelliteMode()

// Custom TileServer
.tileSource(myTileServer)

// Solid color background (no network calls)
.solidBackground(Color.DARK_GRAY)

// Debug tiles showing coordinates
.debugTiles()
```

### Setting Map Dimensions

```java
// By real-world distance (auto-calculates zoom)
.center(location)
.width(Distance.ofNauticalMiles(10))

// By pixel dimensions and zoom level
.center(location)
.width(800, 14)  // 800 pixels wide, zoom level 14
```

### Adding Features

```java
// Single feature
.addFeature(MapFeatures.filledCircle(loc, Color.RED, 10))

// Multiple features
.addFeatures(
    MapFeatures.filledCircle(loc1, Color.RED, 10),
    MapFeatures.filledCircle(loc2, Color.BLUE, 10)
)

// From collection with renderer function
.addFeatures(locations, loc -> MapFeatures.filledCircle(loc, Color.GREEN, 8))

// Using FeatureSet
.addFeatures(featureSet)
```

### Enabling Disk Cache

```java
.useLocalDiskCaching(Duration.ofDays(7))
```

Tiles are cached to `mapTileCache/` directory.

### Output Options

```java
// To BufferedImage
BufferedImage image = builder.toImage();

// To file (PNG or JPG based on extension)
builder.toFile(new File("map.png"));
builder.toFile(new File("map.jpg"));

// Get MapImage for reuse
MapImage mapImage = builder.buildWithoutFeatures();
```

---

## MapImage

Core class that downloads tiles, combines them, and renders features.

### Creating MapImage Directly

```java
// With Distance (auto zoom)
MapImage map = new MapImage(
    tileServer,
    LatLong.of(40.7128, -74.0060),  // center
    Distance.ofMiles(10)            // width
);

// With explicit dimensions
MapImage map = new MapImage(
    tileServer,
    LatLong.of(40.7128, -74.0060),  // center
    800,                             // width in pixels
    14                               // zoom level
);
```

### Rendering

```java
// Base map only
BufferedImage base = map.plot();

// With features
BufferedImage decorated = map.plot(features);

// With multiple feature sets
BufferedImage decorated = map.plot(featureSet1, featureSet2);

// To file
map.plotToFile(features, new File("output.png"));
```

### Accessing Map Properties

```java
LatLong center = map.center();
LatLong topLeft = map.topLeft();
LatLong bottomRight = map.bottomRight();
```

### Quick Visualization

Static convenience method for plotting location data:

```java
List<LatLong> points = getLocations();
Distance mapWidth = Distance.ofMiles(5);

BufferedImage quickMap = MapImage.plotLocationData(points, mapWidth);
```

---

## Efficient Multi-Frame Rendering

For animations or multiple renders of the same area, create the MapImage once and reuse it:

```java
// Create base map once (downloads and caches tiles)
MapImage baseMap = MapBuilder.newMapBuilder()
    .mapBoxDarkMode()
    .center(center)
    .width(Distance.ofMiles(10))
    .buildWithoutFeatures();

// Render multiple frames without re-downloading
for (int frame = 0; frame < 1000; frame++) {
    FeatureSet features = createFeaturesForFrame(frame);
    baseMap.plotToFile(features, new File("frame_" + frame + ".png"));
}
```

---

## Coordinate Systems

### LatLong to Pixels

The library handles coordinate conversion internally using Web Mercator projection.

```java
// PixelLatLong converts between geographic and pixel coordinates
PixelLatLong pixel = new PixelLatLong(latLong, zoomLevel, tileSize);
double x = pixel.x();
double y = pixel.y();

// Convert back
LatLong location = pixel.latLong();
```

### Tile Addressing

Tiles are addressed by (x, y, zoom) coordinates:

```java
// Find tile containing a location
TileAddress tile = TileAddress.of(latLong, zoomLevel);

// Get tiles spanning a region
List<TileAddress> tiles = TileAddress.tileAddressesSpanning(
    topLeft, bottomRight, zoomLevel
);
```

---

## Complete Example

```java
// Data to visualize
LatLong home = LatLong.of(32.8968, -97.0380);
List<LatLong> visited = getVisitedLocations();
List<LatLong> planned = getPlannedRoute();

// Build the map
BufferedImage map = MapBuilder.newMapBuilder()
    .mapBoxDarkMode()
    .useLocalDiskCaching(Duration.ofDays(7))
    .center(home)
    .width(Distance.ofMiles(20))
    // Home marker
    .addFeature(MapFeatures.filledCircle(home, Color.RED, 20))
    // Visited locations
    .addFeatures(visited, loc -> MapFeatures.filledCircle(loc, Color.GREEN, 8))
    // Planned route
    .addFeature(MapFeatures.path(planned, Color.YELLOW, 2.0f))
    .toImage();

// Save
ImageIO.write(map, "PNG", new File("my_map.png"));
```

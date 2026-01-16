# Commons Maps - Getting Started

A Java library for programmatically creating map images with custom overlays and features.

## Overview

Commons Maps provides a fluent API for:

- Downloading and compositing map tiles from various sources
- Drawing geographic features (circles, lines, paths, shapes, text)
- Creating static map images for visualization and analysis

## Key Classes

| Class | Purpose |
|-------|---------|
| `MapBuilder` | Fluent API for creating maps |
| `MapImage` | Core class that renders maps |
| `MapFeatures` | Factory for drawable elements |
| `TileServer` | Interface for tile sources |

## Quick Example

```java
LatLong center = LatLong.of(40.7128, -74.0060);  // NYC

MapBuilder.newMapBuilder()
    .mapBoxDarkMode()                             // Tile source
    .center(center)
    .width(Distance.ofMiles(10))                  // Auto-calculates zoom
    .addFeature(MapFeatures.filledCircle(center, Color.RED, 20))
    .toFile(new File("map.png"));
```

## Workflow

1. **Choose a tile source** - MapBox, solid color, or custom
2. **Set map bounds** - Center point and width
3. **Add features** - Circles, lines, paths, shapes, text
4. **Output** - BufferedImage or file

```
MapBuilder → TileServer → MapImage → BufferedImage/File
                ↓
           Download tiles
                ↓
           Combine & crop
                ↓
           Draw features
```

## Tile Sources

| Source | Description |
|--------|-------------|
| `mapBoxDarkMode()` | Dark themed MapBox tiles |
| `mapBoxLightMode()` | Light themed MapBox tiles |
| `mapBoxSatelliteMode()` | Satellite imagery |
| `solidBackground(Color)` | Solid color (no network) |
| `debugTiles()` | Shows tile coordinates |

## Dependencies

- **commons-units** - LatLong, Distance, Course
- **MapBox API** - For real map tiles (requires token)

## MapBox Token Setup

To use MapBox tiles, provide your access token via:

1. Environment variable: `MAPBOX_ACCESS_TOKEN`
2. System property: `MAPBOX_ACCESS_TOKEN`
3. File: `mapbox.token` in working directory

## Next Steps

- [Map Creation](02-map-creation.md) - MapBuilder and MapImage details
- [Features](03-features.md) - Drawing circles, lines, shapes, text
- [Tile Servers](04-tile-servers.md) - Tile sources and caching

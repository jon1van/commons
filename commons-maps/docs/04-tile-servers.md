# Tile Servers

Map tile sources and caching strategies.

## TileServer Interface

All tile sources implement the `TileServer` interface:

```java
public interface TileServer {
    int maxZoomLevel();
    int maxTileSize();
    URL getUrlFor(TileAddress address);
    BufferedImage downloadMap(TileAddress address);
    BufferedImage downloadAndCombineTiles(List<TileAddress> addresses);
}
```

---

## MapBox API

Real map tiles from MapBox. Requires an API token.

### Available Styles

| Method | Style | Description |
|--------|-------|-------------|
| `mapBoxDarkMode()` | DARK | Dark themed streets |
| `mapBoxLightMode()` | LIGHT | Light themed streets |
| `mapBoxSatelliteMode()` | SATELLITE | Satellite imagery |
| `mapBoxStreets()` | STREETS | Standard street map |
| `mapBoxOutdoors()` | OUTDOORS | Terrain and trails |

### Usage

```java
// Via MapBuilder
MapBuilder.newMapBuilder()
    .mapBoxDarkMode()
    // ...

// Direct instantiation
TileServer mapbox = new MapBoxApi(MapBoxApi.Style.DARK);
```

### Token Configuration

Provide your MapBox access token via one of:

1. **Environment variable**:
   ```bash
   export MAPBOX_ACCESS_TOKEN=pk.your_token_here
   ```

2. **System property**:
   ```bash
   java -DMAPBOX_ACCESS_TOKEN=pk.your_token_here ...
   ```

3. **Token file** (`mapbox.token` in working directory):
   ```
   pk.your_token_here
   ```

### Properties

- Max zoom: 18
- Tile size: 512 pixels

---

## MonochromeTileServer

Solid color background. No network calls required.

### Usage

```java
// Via MapBuilder
MapBuilder.newMapBuilder()
    .solidBackground(Color.DARK_GRAY)
    // ...

// Direct instantiation
TileServer mono = new MonochromeTileServer(Color.BLACK);
```

### Use Cases

- Unit testing without network
- Simple backgrounds for data visualization
- Offline map generation

### Properties

- Max zoom: 20
- Tile size: 512 pixels

---

## DebugTileServer

Shows tile coordinates and zoom level on each tile. Useful for debugging.

### Usage

```java
// Via MapBuilder
MapBuilder.newMapBuilder()
    .debugTiles()
    // ...

// Direct instantiation
TileServer debug = new DebugTileServer();
```

### Output

Each tile displays:
- Zoom level
- X index
- Y index

### Properties

- Max zoom: 20
- Tile size: 512 pixels

---

## LocallyCachingTileServer

Decorator that wraps any TileServer with disk caching.

### Usage

```java
// Via MapBuilder (wraps current tile source)
MapBuilder.newMapBuilder()
    .mapBoxDarkMode()
    .useLocalDiskCaching(Duration.ofDays(7))
    // ...

// Direct instantiation
TileServer cached = new LocallyCachingTileServer(
    new MapBoxApi(MapBoxApi.Style.DARK),
    Duration.ofDays(7)
);
```

### Caching Behavior

- **Two-tier cache**: In-memory (64 tiles) + disk
- **Cache directory**: `mapTileCache/` in working directory
- **File naming**: `{zoom}-{x}-{y}.png`
- **Default retention**: 7 days
- **Automatic cleanup**: Old tiles deleted on access

### Benefits

- Reduces API calls and bandwidth
- Faster rendering for previously viewed areas
- Enables offline use for cached regions
- Respects rate limits

### Cache Management

```java
// Custom retention period
.useLocalDiskCaching(Duration.ofDays(30))

// Short retention for frequently changing data
.useLocalDiskCaching(Duration.ofHours(1))
```

---

## Custom TileServer

Implement `TileServer` for custom tile sources:

```java
public class CustomTileServer implements TileServer {

    @Override
    public int maxZoomLevel() {
        return 18;
    }

    @Override
    public int maxTileSize() {
        return 256;  // or 512
    }

    @Override
    public URL getUrlFor(TileAddress address) {
        String url = String.format(
            "https://my-tile-server.com/tiles/%d/%d/%d.png",
            address.zoomLevel(),
            address.xIndex(),
            address.yIndex()
        );
        return new URL(url);
    }

    @Override
    public BufferedImage downloadMap(TileAddress address) {
        URL url = getUrlFor(address);
        return ImageIO.read(url);
    }

    @Override
    public BufferedImage downloadAndCombineTiles(List<TileAddress> addresses) {
        // Combine multiple tiles into single image
        // ... implementation
    }
}
```

### Using Custom Server

```java
TileServer custom = new CustomTileServer();

MapBuilder.newMapBuilder()
    .tileSource(custom)
    .center(location)
    .width(Distance.ofMiles(5))
    .toImage();
```

---

## Tile Addressing

### TileAddress

Represents a tile's position in the tile grid:

```java
// Find tile containing a location
TileAddress tile = TileAddress.of(latLong, zoomLevel);

int x = tile.xIndex();
int y = tile.yIndex();
int z = tile.zoomLevel();

// URL component: "{zoom}/{x}/{y}"
String urlPart = tile.tileUrlComponent();
```

### Finding Tiles for a Region

```java
// Get all tiles needed to cover a rectangular region
List<TileAddress> tiles = TileAddress.tileAddressesSpanning(
    topLeftLatLong,
    bottomRightLatLong,
    zoomLevel
);
```

### Tile Pixel Coordinates

```java
TileAddress tile = TileAddress.of(location, 14);

// Top-left pixel of this tile
PixelLatLong topLeft = tile.topLeftPixel(512);

// Bottom-right pixel
PixelLatLong bottomRight = tile.bottomRightPixel(512);
```

---

## Zoom Levels

| Zoom | Approximate Coverage | Typical Use |
|------|---------------------|-------------|
| 0 | Entire world | Global view |
| 5 | Large country | Country overview |
| 10 | Large city | Metro area |
| 14 | Neighborhood | Local area |
| 18 | Building level | Street detail |

### Auto Zoom Calculation

When using `Distance` for width, zoom is calculated so the distance spans approximately 2-3 tiles:

```java
// Auto-calculates appropriate zoom
.width(Distance.ofMiles(10))

// Explicit zoom control
.width(800, 14)  // 800 pixels, zoom 14
```

---

## Performance Tips

### Enable Caching

Always use disk caching for production:

```java
.useLocalDiskCaching(Duration.ofDays(7))
```

### Reuse MapImage

For animations or multiple renders, create MapImage once:

```java
MapImage base = MapBuilder.newMapBuilder()
    .mapBoxDarkMode()
    .center(center)
    .width(Distance.ofMiles(10))
    .buildWithoutFeatures();

// Render many frames without re-downloading
for (int i = 0; i < 1000; i++) {
    base.plotToFile(frameFeatures[i], new File("frame_" + i + ".png"));
}
```

### Use MonochromeTileServer for Testing

```java
// Fast tests without network
MapBuilder.newMapBuilder()
    .solidBackground(Color.GRAY)
    .center(testLocation)
    .width(400, 10)
    .addFeatures(testFeatures)
    .toImage();
```

### Choose Appropriate Zoom

Lower zoom = fewer tiles = faster rendering:

```java
// Zoom 10: ~4 tiles for city view
// Zoom 14: ~16 tiles for neighborhood
// Zoom 18: ~64+ tiles for street level
```

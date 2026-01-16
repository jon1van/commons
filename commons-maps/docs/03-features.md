# Map Features

Drawing circles, lines, paths, shapes, and text on maps.

## MapFeatures Factory

Static factory methods for creating drawable map elements.

### Circles

```java
// Filled circle
MapFeature dot = MapFeatures.filledCircle(
    LatLong.of(40.7128, -74.0060),  // center
    Color.RED,                       // color
    15                               // diameter in pixels
);

// Circle outline
MapFeature ring = MapFeatures.circle(
    LatLong.of(40.7128, -74.0060),  // center
    Color.BLUE,                      // color
    20,                              // diameter in pixels
    2.0f                             // stroke width
);

// Batch circles (more efficient for many)
Collection<LatLong> locations = getLocations();
MapFeature dots = MapFeatures.circles(
    locations,
    Color.GREEN,
    10,      // diameter
    1.5f     // stroke width
);
```

### Lines

```java
// Single line segment
MapFeature line = MapFeatures.line(
    LatLong.of(40.7128, -74.0060),  // from
    LatLong.of(40.7500, -74.0100),  // to
    Color.WHITE,                     // color
    2.0f                             // line width
);
```

### Paths (Polylines)

```java
// Path connecting multiple points
List<LatLong> waypoints = List.of(
    LatLong.of(40.7128, -74.0060),
    LatLong.of(40.7300, -74.0000),
    LatLong.of(40.7500, -74.0100)
);

MapFeature route = MapFeatures.path(
    waypoints,
    Color.YELLOW,
    3.0f       // stroke width
);

// From LatLongPath
LatLongPath track = getTrack();
MapFeature trackLine = MapFeatures.path(track, Color.CYAN, 2.0f);
```

### Shapes (Polygons)

```java
List<LatLong> vertices = List.of(
    LatLong.of(40.70, -74.01),
    LatLong.of(40.72, -74.00),
    LatLong.of(40.71, -73.98),
    LatLong.of(40.69, -73.99)
);

// Outline only
MapFeature outline = MapFeatures.shape(vertices, Color.WHITE);

// Filled shape
MapFeature filled = MapFeatures.filledShape(vertices, new Color(255, 0, 0, 128));

// Both fill and outline
List<MapFeature> both = MapFeatures.shapeWithOutline(
    vertices,
    new Color(0, 255, 0, 100),  // fill color (with alpha)
    Color.GREEN                  // border color
);
```

### Rectangles

```java
MapFeature rect = MapFeatures.rect(
    LatLong.of(40.72, -74.02),   // top-left
    LatLong.of(40.70, -74.00),   // bottom-right
    Color.ORANGE,
    2.0f                          // line width
);
```

### Text

```java
// Geographically anchored text
MapFeature label = MapFeatures.text(
    "New York City",
    LatLong.of(40.7128, -74.0060),  // anchor location
    Color.WHITE
);

// With custom font
MapFeature styled = MapFeatures.text(
    "NYC",
    LatLong.of(40.7128, -74.0060),
    Color.WHITE,
    new Font("Arial", Font.BOLD, 16)
);

// Screen-space anchored (fixed position)
MapFeature corner = MapFeatures.text(
    "Map Legend",
    10,           // x offset from left
    20,           // y offset from top
    Color.WHITE
);

// Multi-line text
String[] lines = {"Line 1", "Line 2", "Line 3"};
MapFeature multiLine = MapFeatures.textLines(
    lines,
    10,                              // x offset
    20,                              // y offset
    Color.WHITE,
    new Font("Monospaced", Font.PLAIN, 12),
    15                               // line spacing
);
```

### Composing Features

```java
// Combine multiple features into one
MapFeature combined = MapFeatures.compose(List.of(
    MapFeatures.filledCircle(loc, Color.RED, 20),
    MapFeatures.text("Label", loc, Color.WHITE)
));
```

---

## FeatureSetBuilder

Fluent builder for creating collections of features with shared styling.

### Basic Usage

```java
FeatureSet features = FeatureSetBuilder.newFeatureSetBuilder()
    .setColor(Color.BLUE)
    .setCircleDiameter(10)
    .setStrokeWidth(2.0f)
    .addCircle(loc1)
    .addCircle(loc2)
    .addCircle(loc3)
    .build();
```

### Setting Default Styles

```java
FeatureSetBuilder builder = FeatureSetBuilder.newFeatureSetBuilder()
    .setColor(Color.RED)           // Default color
    .setCircleDiameter(15)         // Default circle size
    .setStrokeWidth(2.5f)          // Default line width
    .setFont(new Font("Arial", Font.BOLD, 14));
```

### Adding Features

```java
FeatureSet features = FeatureSetBuilder.newFeatureSetBuilder()
    // Circles (use defaults or override)
    .addCircle(loc1)                              // Uses defaults
    .addCircle(loc2, Color.GREEN)                 // Override color
    .addCircle(loc3, Color.BLUE, 20)              // Override color and size
    .addCircle(loc4, Color.YELLOW, 25, 3.0f)      // Override all

    // Filled circles
    .addFilledCircle(loc5)
    .addFilledCircle(loc6, Color.MAGENTA)
    .addFilledCircle(loc7, Color.CYAN, 12)

    // Lines
    .addLine(from, to)
    .addLine(from, to, Color.WHITE)
    .addLine(from, to, Color.WHITE, 4.0f)

    // Paths
    .addPath(waypoints, Color.YELLOW, 2.0f)

    // Shapes
    .addShape(vertices, Color.WHITE)
    .addFilledShape(vertices, Color.RED)

    // Text
    .addText("Label", location)
    .addText("Styled", location, Color.WHITE, customFont)

    // Pre-built features
    .addFeature(existingFeature)

    .build();
```

### Complete Example

```java
LatLong home = LatLong.of(40.7128, -74.0060);
List<LatLong> route = getRoute();
List<LatLong> waypoints = getWaypoints();

FeatureSet features = FeatureSetBuilder.newFeatureSetBuilder()
    // Home marker
    .setColor(Color.RED)
    .addFilledCircle(home, Color.RED, 25)
    .addText("Home", home, Color.WHITE)

    // Route line
    .addPath(route, Color.YELLOW, 3.0f)

    // Waypoint markers
    .setColor(Color.CYAN)
    .setCircleDiameter(12)
    .addFilledCircle(waypoints.get(0))
    .addFilledCircle(waypoints.get(1))
    .addFilledCircle(waypoints.get(2))

    .build();

MapBuilder.newMapBuilder()
    .mapBoxDarkMode()
    .center(home)
    .width(Distance.ofMiles(10))
    .addFeatures(features)
    .toFile(new File("route.png"));
```

---

## Custom MapFeature

Implement the `MapFeature` interface for custom drawing:

```java
public class CustomMarker implements MapFeature {
    private final LatLong location;
    private final String label;

    public CustomMarker(LatLong location, String label) {
        this.location = location;
        this.label = label;
    }

    @Override
    public void drawOn(Graphics2D g, PixelLatLong zeroPixel) {
        // Convert geographic to pixel coordinates
        PixelLatLong pixel = new PixelLatLong(
            location,
            zeroPixel.zoom(),
            zeroPixel.tileSize()
        );

        int x = (int) pixel.x(zeroPixel);
        int y = (int) pixel.y(zeroPixel);

        // Draw custom graphics
        g.setColor(Color.RED);
        g.fillOval(x - 10, y - 10, 20, 20);

        g.setColor(Color.WHITE);
        g.drawString(label, x + 15, y + 5);
    }
}
```

Usage:

```java
MapBuilder.newMapBuilder()
    .mapBoxDarkMode()
    .center(location)
    .width(Distance.ofMiles(5))
    .addFeature(new CustomMarker(location, "Custom!"))
    .toImage();
```

---

## Transparency and Colors

Use `Color` with alpha channel for transparency:

```java
// Semi-transparent red (50% opacity)
Color semiRed = new Color(255, 0, 0, 128);

// Fully transparent
Color transparent = new Color(0, 0, 0, 0);

// Using with features
MapFeature overlay = MapFeatures.filledShape(
    vertices,
    new Color(0, 100, 255, 80)  // Light blue, ~30% opacity
);
```

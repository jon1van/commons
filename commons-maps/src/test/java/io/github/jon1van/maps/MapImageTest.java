package io.github.jon1van.maps;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.maps.FeatureSetBuilder.newFeatureSetBuilder;
import static io.github.jon1van.maps.MapBoxApi.Style.DARK;
import static io.github.jon1van.units.Course.*;
import static org.assertj.core.api.Assertions.*;

import java.awt.Color;
import java.io.File;
import java.util.List;

import io.github.jon1van.units.Distance;
import io.github.jon1van.units.LatLong;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MapImageTest {

    @TempDir
    public File tempDir;

    @Disabled
    @Test
    public void drawSimpleMapWithNoFeatures() {
        // Disabled because (1) downloaded map data takes a while, (2) tiles change output map...so we can't test
        // against image equality

        MapImage map = new MapImage(
                new MapBoxApi(DARK), // tile server
                LatLong.of(38.9223, -77.2016), // center point
                Distance.ofNauticalMiles(2.5) // map width
                );

        map.plotToFile(new File("mapWithoutDecoration.jpg"));
    }

    @Disabled
    @Test
    public void drawMapWithAdditionalFeatures() {
        // Disabled because (1) downloaded map data takes a while, (2) tile server changes output...so we can't test
        // against image equality

        LatLong lostDog = LatLong.of(38.9223, -77.2016);

        MapImage map = new MapImage(
                new MapBoxApi(DARK), // tile server
                lostDog, // center point
                Distance.ofNauticalMiles(2.5) // map width
                );

        // create a list of MapFeatures that need to be drawn...
        FeatureSet features = newFeatureSetBuilder()
                .addCircle(lostDog, Color.RED, 30, 4.0f)
                .addLine(
                        lostDog.move(NORTH, Distance.ofNauticalMiles(1.0)),
                        lostDog.move(SOUTH, Distance.ofNauticalMiles(1.0)),
                        Color.MAGENTA,
                        1.f)
                .addFilledShape(boxAround(lostDog), new Color(255, 255, 255, 25)) // use Alpha channel for transparency!
                .build();

        map.plotToFile(features, new File("mapWithDecoration.jpg"));
    }

    @Disabled
    @Test
    public void drawMovingDot() {
        // Disabled because (1) downloaded map data takes a while, (2) tile server changes output...so we can't test
        // against image equality

        LatLong lostDog = LatLong.of(38.9223, -77.2016);

        MapImage map = new MapImage(
                new MapBoxApi(DARK), // tile server
                lostDog, // center point
                Distance.ofNauticalMiles(2.5) // map width
                );

        for (int i = 0; i < 10; i++) {
            drawMovieFrame(lostDog, map, i);
        }
    }

    private void drawMovieFrame(LatLong lostDog, MapImage map, int i) {

        FeatureSet features = newFeatureSetBuilder()
                .addCircle(lostDog, Color.RED, 30, 4.0f)
                .setStrokeWidth(3.0f)
                .addCircle(lostDog.move(NORTH, Distance.ofNauticalMiles(.1).times(i)), Color.BLUE, 40)
                .build();

        map.plotToFile(features, new File("movingDot_" + i + ".jpg"));
    }

    // find a "diamond of LatLongs" around a center point
    private List<LatLong> boxAround(LatLong center) {
        return newArrayList(
                center.move(NORTH, Distance.ofNauticalMiles(1)),
                center.move(EAST, Distance.ofNauticalMiles(1)),
                center.move(SOUTH, Distance.ofNauticalMiles(1)),
                center.move(WEST, Distance.ofNauticalMiles(1)));
    }

    @Test
    public void mapImagesDefinedByPixelSizeAreThatPixelSize() {

        int WIDTH = 640;

        MapImage map = new MapImage(
                new DebugTileServer(),
                LatLong.of(38.9223, -77.2016), // center point
                WIDTH,
                12 // zoom
                );

        assertThat(map.plot().getWidth()).isEqualTo(WIDTH);
        assertThat(map.plot().getHeight()).isEqualTo(WIDTH);

        map.plotToFile(new File(tempDir, "640x640Map.jpg"));
    }
}

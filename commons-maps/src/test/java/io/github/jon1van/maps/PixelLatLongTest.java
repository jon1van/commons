package io.github.jon1van.maps;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import io.github.jon1van.units.LatLong;
import org.junit.jupiter.api.Test;

class PixelLatLongTest {

    @Test
    public void basicPixelLatLongConstruction() {

        LatLong dfw = LatLong.of(32.897480, -97.040443);
        int zoom = 13;
        int tileSize = 512;

        PixelLatLong pll = new PixelLatLong(dfw, zoom, tileSize);

        assertThat(pll.x()).isCloseTo(966549, within(5.0));
        assertThat(pll.y()).isCloseTo(1690888, within(5.0));
    }

    @Test
    public void circularPixelLatLongConstruction() {

        LatLong dfw = LatLong.of(32.897480, -97.040443);
        int zoom = 13;
        int tileSize = 512;

        PixelLatLong pll = new PixelLatLong(dfw, zoom, tileSize);

        PixelLatLong round2 = new PixelLatLong(pll.x(), pll.y(), zoom, tileSize);

        assertThat(round2.latLong().latitude()).isCloseTo(32.897480, within(0.001));
        assertThat(round2.latLong().longitude()).isCloseTo(-97.040443, within(0.001));

        assertThat(round2.x()).isCloseTo(966549, within(5.0));
        assertThat(round2.y()).isCloseTo(1690888, within(5.0));

        assertThat(pll.x()).isCloseTo(round2.x(), within(1.0));
        assertThat(pll.y()).isCloseTo(round2.y(), within(1.0));
    }
}

package io.github.jon1van.commons.units;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.commons.units.HasLatLong.*;
import static org.junit.jupiter.api.Assertions.*;


class HasLatLongTest {

    @Test
    public void testDistanceInNmTo() {

        PositionHaver one = new PositionHaver(LatLong.of(0.0, 0.0));
        PositionHaver two = new PositionHaver(LatLong.of(1.0, 1.0));

        double EXPECTED_DIST_IN_KM = 157.2;
        double KM_PER_NM = 1.852;
        double expectedDistance = EXPECTED_DIST_IN_KM / KM_PER_NM;
        double actualDistance = one.distanceInNmTo(two);

        double TOLERANCE = 0.1;

        assertEquals(expectedDistance, actualDistance, TOLERANCE);
    }

    @Test
    public void testMinMaxMethods() {

        PositionHaver v1 = new PositionHaver(LatLong.of(40.75, -73.9));
        PositionHaver v2 = new PositionHaver(LatLong.of(40.75, -74.1));
        PositionHaver v3 = new PositionHaver(LatLong.of(40.7, -74.1));
        PositionHaver v4 = new PositionHaver(LatLong.of(40.7, -73.9));

        List<PositionHaver> points = newArrayList(v1, v2, v3, v4);

        double TOLERANCE = 0.001;
        assertEquals(40.7, minLatitude(points), TOLERANCE);
        assertEquals(40.75, maxLatitude(points), TOLERANCE);
        assertEquals(-74.1, minLongitude(points), TOLERANCE);
        assertEquals(-73.9, maxLongitude(points), TOLERANCE);
    }

    public static class PositionHaver implements HasLatLong {

        LatLong location;

        PositionHaver(LatLong location) {
            this.location = location;
        }

        @Override
        public double latitude() {
            return location.latitude();
        }

        @Override
        public double longitude() {
            return location.longitude();
        }

    }
}

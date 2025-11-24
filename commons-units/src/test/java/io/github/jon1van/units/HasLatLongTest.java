package io.github.jon1van.units;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.units.HasLatLong.*;
import static io.github.jon1van.units.Navigation.EARTH_RADIUS_NM;
import static java.lang.Math.PI;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class HasLatLongTest {

    @Test
    void testAvgLatLong_onLatLongList() {

        List<LatLong> list = List.of(LatLong.of(0.0, 10.0), LatLong.of(10.0, 20.0));
        // Not checking accuracy, this was failing previously due to a casting error
        assertDoesNotThrow(() -> HasLatLong.avgLatLong(list));
    }

    @Test
    public void testAvgLatLong() {
        LatLong128 one = LatLong128.of(0.0, 10.0);
        LatLong128 two = LatLong128.of(10.0, 20.0);
        LatLong128 naiveAverage = LatLong128.of(5.0, 15.0); // naive arthimatic average of LatLong
        var correctAverage = avgLatLong(one, two);

        Distance oneToAvg = one.distanceTo(correctAverage);
        Distance avgToTwo = correctAverage.distanceTo(two);
        Distance realToNaive = correctAverage.distanceTo(naiveAverage);

        assertEquals(oneToAvg.inNauticalMiles(), avgToTwo.inNauticalMiles(), 0.00001);

        // the naive answer is off by over 2.5 NM !
        assertThat(realToNaive.isGreaterThan(Distance.ofNauticalMiles(2.5))).isTrue();
    }

    @Test
    public void testAvgLatLong_poleToPole() {
        LatLong128 north = LatLong128.of(89.0, 0.0); // near north pole
        LatLong128 south = LatLong128.of(-89.5, 0.0); // near south pole

        var avg = avgLatLong(north, south);

        Distance oneToTwo = north.distanceTo(south);

        Distance halfEarthCircumfrence = Distance.ofNauticalMiles(PI * EARTH_RADIUS_NM);

        // these values are similar...but halfEarthCircumfrence is slightly larger
        assertThat(oneToTwo.dividedBy(halfEarthCircumfrence)).isGreaterThan(.95);
        assertThat(halfEarthCircumfrence.dividedBy(oneToTwo)).isLessThan(1.05);

        // the average is the same distance to both the northern and southern points
        assertEquals(
                avg.distanceTo(north).inNauticalMiles(), avg.distanceTo(south).inNauticalMiles(), 0.001);
    }

    @Test
    public void testAvgLatLong_acrossDateLine() {

        LatLong128 east = LatLong128.of(0.0, -179.5); // just east of line
        LatLong128 west = LatLong128.of(0.0, 179.0); // just west of line
        LatLong128 niaveAverage = LatLong128.of(0.0, -0.25); // niave arthimatic average of LatLong
        var correctAverage = avgLatLong(east, west);

        Distance distBtwPoints = east.distanceTo(west);

        // the distance between the east and west points is small (about 90.01 NM)
        assertThat(distBtwPoints.isLessThan(Distance.ofNauticalMiles(91.0))).isTrue();
        assertThat(distBtwPoints.isGreaterThan(Distance.ofNauticalMiles(89.0))).isTrue();

        // the distance between the average point and the east point is about 45.5 NM
        assertThat(correctAverage.distanceTo(east).isLessThan(Distance.ofNauticalMiles(45.5)))
                .isTrue();
        assertThat(correctAverage.distanceTo(east).isGreaterThan(Distance.ofNauticalMiles(44.5)))
                .isTrue();

        // the distance between the average point and the west point is about 45.5 NM
        assertThat(correctAverage.distanceTo(west).isLessThan(Distance.ofNauticalMiles(45.5)))
                .isTrue();
        assertThat(correctAverage.distanceTo(west).isGreaterThan(Distance.ofNauticalMiles(44.5)))
                .isTrue();

        // the distance from average to east = distance from average to west
        assertEquals(
                correctAverage.distanceTo(east).inNauticalMiles(),
                correctAverage.distanceTo(west).inNauticalMiles(),
                0.001);

        // the naive answer is literally on the otherside of the planet
        Distance error = correctAverage.distanceTo(niaveAverage);
        assertThat(error.isGreaterThan(Distance.ofNauticalMiles(10_801))).isTrue();
    }

    @Test
    public void testAvgLatLong_array() {

        LatLong128 one = LatLong128.of(0.0, -179.5); // just east of date-line
        LatLong128 two = LatLong128.of(0.0, 179.0); // just west of date-line
        LatLong128 three = LatLong128.of(1.0, 179.0); // due north of two

        LatLong realAverage = avgLatLong(one, two, three);
        LatLong128 manualAverage = LatLong128.of(0.33, 179.5);

        // the real average is NOT the manual average
        assertThat(realAverage.distanceTo(manualAverage).isGreaterThan(Distance.ofNauticalMiles(0)))
                .isTrue();

        // distance are small -- so error won't be too big assuming the internation date line doesn't hose the
        // computation
        assertThat(realAverage.latitude() > .33 && realAverage.latitude() < .34).isTrue();
        assertThat(realAverage.longitude() > 179.5 && realAverage.longitude() < 179.51)
                .isTrue();
    }

    @Test
    public void testQuickAvgLatLong_1() {

        LatLong128 east = LatLong128.of(0.0, -179.5); // just east of line
        LatLong128 west = LatLong128.of(0.0, 179.0); // just west of line
        var quickAverage = quickAvgLatLong(east, west);
        LatLong128 expected = LatLong128.of(0.0, 179.750);

        assertThat(quickAverage.distanceTo(expected).isLessThan(Distance.ofNauticalMiles(0.001)))
                .isTrue();
    }

    @Test
    public void testQuickAvgLatLong_2() {

        LatLong128 east = LatLong128.of(-11.0, 12.5);
        LatLong128 west = LatLong128.of(15.0, -42.0);
        var quickAverage = quickAvgLatLong(east, west);
        LatLong128 expected = LatLong128.of(2.0, -14.75);

        assertThat(quickAverage.distanceTo(expected).isLessThan(Distance.ofNauticalMiles(0.001)))
                .isTrue();
    }

    @Test
    public void testAvgLatLong_similarResults_differentMethods() {

        LatLong128 one = LatLong128.of(42.93675, -83.70776);
        LatLong128 two = LatLong128.of(42.95037, -83.70570);

        // compute the solution two ways
        var quickAverage = quickAvgLatLong(one, two);
        var accurateAverage = avgLatLong(one, two);

        // solutions a NOT the same...but they are damn near indistinguishable
        assertNotEquals(quickAverage, accurateAverage);
        assertThat(quickAverage.distanceTo(accurateAverage).isLessThan(Distance.ofNauticalMiles(0.00005)))
                .isTrue();
    }

    @Test
    public void quickAvgLatLong_simple() {

        // These points are 846.45952 Nautical Miles apart!
        // The "naive average location" will be WRONG
        LatLong128 one = LatLong128.of(0.0, 10.0);
        LatLong128 two = LatLong128.of(10.0, 20.0);

        ArrayList<LatLong128> path = newArrayList(one, two);

        var average = avgLatLong(path); // accurately computed avg LatLong
        var naiveAverage = LatLong.of(5.0, 15.0); // naive arithmetic average of LatLong

        assertThat(quickAvgLatLong(path)).isEqualTo(naiveAverage);

        // the naive answer is off by over 2.5 NM!
        Distance realToNaive = average.distanceTo(naiveAverage);
        assertThat(realToNaive.isGreaterThan(Distance.ofNauticalMiles(2.5))).isTrue();
    }

    @Test
    public void testQuickAvgLatLong_acrossDateLine() {

        LatLong128 east = LatLong128.of(0.0, -179.5); // just east of line
        LatLong128 west = LatLong128.of(0.0, 179.0); // just west of line

        ArrayList<LatLong128> path = newArrayList(east, west);

        LatLong128 naiveAverage = LatLong128.of(0.0, -0.25); // naive arithmetic average of LatLong
        var correctAverage = quickAvgLatLong(path); // (0.0,179.75)

        Distance distBtwPoints = east.distanceTo(west);

        // the distance between the east and west points is small (about 90.01 NM)
        assertThat(distBtwPoints.isLessThan(Distance.ofNauticalMiles(91.0))).isTrue();
        assertThat(distBtwPoints.isGreaterThan(Distance.ofNauticalMiles(89.0))).isTrue();

        // the distance between the average point and the east point is about 45.5 NM
        assertThat(correctAverage.distanceTo(east).isLessThan(Distance.ofNauticalMiles(45.5)))
                .isTrue();
        assertThat(correctAverage.distanceTo(east).isGreaterThan(Distance.ofNauticalMiles(44.5)))
                .isTrue();

        // the distance between the average point and the west point is about 45.5 NM
        assertThat(correctAverage.distanceTo(west).isLessThan(Distance.ofNauticalMiles(45.5)))
                .isTrue();
        assertThat(correctAverage.distanceTo(west).isGreaterThan(Distance.ofNauticalMiles(44.5)))
                .isTrue();

        // the distance from average to east = distance from average to west
        assertEquals(
                correctAverage.distanceTo(east).inNauticalMiles(),
                correctAverage.distanceTo(west).inNauticalMiles(),
                0.001);

        // the naive answer is literally on the other side of the planet
        Distance error = correctAverage.distanceTo(naiveAverage);
        assertThat(error.isGreaterThan(Distance.ofNauticalMiles(10_801))).isTrue();
    }

    @Test
    public void testQuickAvgLatLong_acrossDateLine_2() {

        LatLong east = LatLong.of(0.0, -179.5); // just east of line  (aka "180 - .5")
        LatLong west_1 = LatLong.of(0.0, 179.0); // just west of line (aka "180 + 1")
        LatLong west_2 = LatLong.of(0.0, 179.0); // just west of line (aka "180 + 1")

        ArrayList<LatLong> path_1 = newArrayList(east, west_1);
        ArrayList<LatLong> path_2 = newArrayList(east, west_1, west_2);

        var midPoint = quickAvgLatLong(path_1); // should be midpoint
        var twoThirdPoint = quickAvgLatLong(path_2); // should be 2/3rds towards west

        assertThat(midPoint).isEqualTo(LatLong.of(0.0, 179.75));
        assertThat(twoThirdPoint).isEqualTo(LatLong.of(0.0, 179.5));

        Distance distBtwPoints = east.distanceTo(west_1);

        // the distance between the east and west points is small (about 90.01 NM)
        assertThat(distBtwPoints.isLessThan(Distance.ofNauticalMiles(90.1))).isTrue();
        assertThat(distBtwPoints.isGreaterThan(Distance.ofNauticalMiles(90.0))).isTrue();

        // the distance between the average point and the east point is about 45.5 NM
        assertEquals(midPoint.distanceTo(east).inNauticalMiles(), 45.005, 0.001);

        // the distance between the average point and the west point is about 45.005 NM
        assertEquals(midPoint.distanceTo(west_1).inNauticalMiles(), 45.005, 0.001);

        // the distance from average to east = distance from average to west
        assertEquals(
                midPoint.distanceTo(east).inNauticalMiles(),
                midPoint.distanceTo(west_1).inNauticalMiles(),
                0.001);

        // the distance between the 2/3rd point and the east point is about 60.006 NM
        assertEquals(twoThirdPoint.distanceTo(east).inNauticalMiles(), 60.006, 0.001);

        // the distance between the 2/3rd point and the west point is about 30.003 NM
        assertEquals(twoThirdPoint.distanceTo(west_1).inNauticalMiles(), 30.003, 0.001);

        // the distance from 2/3rd point to east is twice the distance from 2/3rd point to west
        assertEquals(
                twoThirdPoint.distanceTo(east).inNauticalMiles(),
                twoThirdPoint.distanceTo(west_1).inNauticalMiles() * 2,
                0.001);
    }

    @Test
    public void testAvgLatLong_similarResults_differentMethods_longerPaths() {

        // total path length = 254.52942NM
        ArrayList<LatLong128> path = newArrayList(
                LatLong128.of(0.0, 0.1), LatLong128.of(1.0, 1.1), LatLong128.of(2.0, 2.1), LatLong128.of(3.0, 3.1));

        // compute the solution two ways ..
        LatLong quickAverage = quickAvgLatLong(path);
        LatLong accurateAverage = avgLatLong(path);

        // solutions a NOT the same...but they are very similar given the length of the path involved
        assertNotEquals(quickAverage, accurateAverage);

        Distance leg1 = path.get(0).distanceTo(path.get(1));
        Distance leg2 = path.get(1).distanceTo(path.get(2));
        Distance leg3 = path.get(2).distanceTo(path.get(3));
        Distance pathDist = leg1.plus(leg2).plus(leg3); // 254.52942NM

        Distance delta = quickAverage.distanceTo(accurateAverage); // 0.03832NM

        assertThat(delta.inNauticalMiles() / pathDist.inNauticalMiles()).isLessThan(0.001);
    }

    @Test
    public void avgLatLong_empty_collection() {
        /*
         * No change in behavior between avgLatLong and quickAvgLatLong
         */

        ArrayList<LatLong128> locations = newArrayList();

        assertThrows(NoSuchElementException.class, () -> avgLatLong(locations));

        assertThrows(NoSuchElementException.class, () -> quickAvgLatLong(locations));
    }

    @Test
    public void avgLatLong_empty_arrays() {
        // avgLatLong and quickAvgLatLong have same behavior
        LatLong128[] locations = new LatLong128[0];
        assertThrows(NoSuchElementException.class, () -> avgLatLong(locations));
        assertThrows(NoSuchElementException.class, () -> quickAvgLatLong(locations));
    }

    @Test
    public void avgLatLong_null_collection() {
        // avgLatLong and quickAvgLatLong have same behavior
        ArrayList<LatLong128> locations = null;
        assertThrows(NullPointerException.class, () -> avgLatLong(locations));
        assertThrows(NullPointerException.class, () -> quickAvgLatLong(locations));
    }

    @Test
    public void avgLatLong_null_arrays() {
        // avgLatLong and quickAvgLatLong have same behavior
        LatLong128[] locations = null;
        assertThrows(NullPointerException.class, () -> avgLatLong(locations));
        assertThrows(NullPointerException.class, () -> quickAvgLatLong(locations));
    }

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

        final LatLong location;

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

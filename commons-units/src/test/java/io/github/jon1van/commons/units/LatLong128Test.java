package io.github.jon1van.commons.units;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.commons.units.HasLatLong.*;
import static io.github.jon1van.commons.units.LatLong128.*;
import static io.github.jon1van.commons.units.Spherical.EARTH_RADIUS_NM;
import static java.lang.Math.PI;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;


public class LatLong128Test {

    @TempDir
    public File tempDir;

    @Test
    public void testCheckBounds_latTooBig() {
        assertThrows(IllegalArgumentException.class, () -> LatLong128.of(90.1, 0.0));
    }

    @Test
    public void testCheckBounds_latTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> LatLong128.of(-90.1, 0.0));
    }

    @Test
    public void testCheckBounds_longTooBig() {
        assertThrows(IllegalArgumentException.class, () -> LatLong128.of(0.0, 180.1));
    }

    @Test
    public void testCheckBounds_longTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> LatLong128.of(0.0, -180.1));
    }

    @Test
    void clampLatitude_spec() {
        assertThat(clampLatitude(Double.NEGATIVE_INFINITY)).isEqualTo(-90.0);
        assertThat(clampLatitude(-90.001)).isEqualTo(-90.0);
        assertThat(clampLatitude(-90.0)).isEqualTo(-90.0);
        assertThat(clampLatitude(-89.999)).isEqualTo(-89.999);

        assertThat(clampLatitude(Double.POSITIVE_INFINITY)).isEqualTo(90.0);
        assertThat(clampLatitude(90.001)).isEqualTo(90.0);
        assertThat(clampLatitude(90.00)).isEqualTo(90.0);
        assertThat(clampLatitude(89.999)).isEqualTo(89.999);
    }

    @Test
    void clampLongitude_spec() {
        assertThat(clampLongitude(Double.NEGATIVE_INFINITY)).isEqualTo(-180.0);
        assertThat(clampLongitude(-180.001)).isEqualTo(-180.0);
        assertThat(clampLongitude(-180.0)).isEqualTo(-180.0);
        assertThat(clampLongitude(-179.999)).isEqualTo(-179.999);

        assertThat(clampLongitude(Double.POSITIVE_INFINITY)).isEqualTo(180.0);
        assertThat(clampLongitude(180.01)).isEqualTo(180.0);
        assertThat(clampLongitude(180.00)).isEqualTo(180.0);
        assertThat(clampLongitude(179.999)).isEqualTo(179.999);
    }

    @Test
    public void testEqualsAndHashcode() {
        LatLong128 one = new LatLong128(45.0, 45.0);
        LatLong128 two = new LatLong128(45.0, 45.0);
        LatLong128 three = new LatLong128(45.0, 46.0);
        LatLong128 four = new LatLong128(46.0, 45.0);
        LatLong128 five = new LatLong128(46.0, 46.0);

        String other = "I am not a LatLong Obj";
        String nullObject = null;

        assertEquals(one, one);

        assertEquals(one, two);
        assertEquals(two, one);

        assertNotEquals(one, three);
        assertNotEquals(three, one);

        assertNotEquals(one, four);
        assertNotEquals(four, one);

        assertNotEquals(one, five);
        assertNotEquals(five, one);

        assertNotEquals(one, other);
        assertNotEquals(other, one);

        assertNotEquals(one, nullObject);

        assertEquals(one.hashCode(), two.hashCode());
        assertNotEquals(one.hashCode(), three.hashCode());
    }

    @Test
    public void testBothConstructors() {
        LatLong128 a = new LatLong128(15.21, 32.5);
        LatLong128 b = LatLong128.of(15.21, 32.5);

        assertThat(a.equals(b)).isTrue();  // they ARE equal
        assertThat(a == b).isFalse(); // but they ARE DIFFERENT instances
    }


    @Test
    public void testToString() {
        LatLong128 instance = new LatLong128(15.0, 22.0);

        String toString = instance.toString();

        assertThat(toString).contains("15.0");
        assertThat(toString).contains("22.0");
        assertThat(toString.indexOf("15.0") < toString.indexOf("22.0")).isTrue(); // latitude comes first
    }

    @Test
    public void testToBytesAndBack() {

        LatLong128 instance = new LatLong128(15.0, 22.0);

        byte[] asBytes = instance.toBytes();

        LatLong128 instanceRemake = LatLong128.fromBytes(asBytes);

        assertThat(instance).isEqualTo(instanceRemake);
        assertThat(instance.latitude()).isEqualTo(instanceRemake.latitude());
        assertThat(instance.longitude()).isEqualTo(instanceRemake.longitude());
    }

    @Test
    public void testBase64Encoding() {

        Random rng = new Random(17L);
        int N = 50;

        for (int i = 0; i < N; i++) {
            double lat = rng.nextDouble() * 10;
            double lon = rng.nextDouble() * 20;
            LatLong128 in = LatLong128.of(lat, lon);
            String asBase64 = in.toBase64();
            LatLong128 out = LatLong128.fromBase64Str(asBase64);

            assertThat(in).isEqualTo(out);
            assertThat(in.latitude()).isEqualTo(out.latitude());
            assertThat(in.longitude()).isEqualTo(out.longitude());
        }
    }

    @Test
    public void testDistanceTo() {
        LatLong128 one = new LatLong128(0.0, 0.0);
        LatLong128 two = new LatLong128(1.0, 1.0);

        double EXPECTED_DIST_IN_KM = 157.2;

        Distance expectedDist = Distance.ofKiloMeters(EXPECTED_DIST_IN_KM);
        Distance actualDistance = one.distanceTo(two);

        double TOLERANCE = 0.1;

        assertEquals(expectedDist.inNauticalMiles(), actualDistance.inNauticalMiles(), TOLERANCE);
    }

    @Test
    public void testDistanceTo_Triangle() {
        LatLong128 point_a = new LatLong128(0.0, 0.0);
        LatLong128 point_b = new LatLong128(0.0, 1.0);
        LatLong128 point_c = new LatLong128(1.0, 0.0);

        double leg_one = point_a.distanceTo(point_b).inMiles();
        double leg_two = point_a.distanceTo(point_c).inMiles();
        double hypotenuse = point_b.distanceTo(point_c).inMiles();

        // for right triangle on sphere, the square of the hypotenuse <= sum of squares of legs
        assertThat(hypotenuse * hypotenuse).isLessThanOrEqualTo(leg_one * leg_one + leg_two * leg_two);
    }

    @Test
    public void testDistanceInNM() {
        LatLong128 one = new LatLong128(0.0, 0.0);
        LatLong128 two = new LatLong128(1.0, 1.0);

        double EXPECTED_DIST_IN_KM = 157.2;
        double KM_PER_NM = 1.852;
        double expectedDistance = EXPECTED_DIST_IN_KM / KM_PER_NM;
        double actualDistance = one.distanceInNmTo(two);

        double TOLERANCE = 0.1;

        assertEquals(expectedDistance, actualDistance, TOLERANCE);
    }

    @Test
    public void testCourseInDegrees() {
        LatLong128 one = new LatLong128(0.0, 0.0);
        LatLong128 two = new LatLong128(1.0, 1.0);

        double TOLERANCE = 0.1;

        assertEquals(45.0, one.courseInDegrees(two), TOLERANCE);
    }

    @Test
    public void testProjectOut() {

        LatLong128 source = new LatLong128(0.0, 0.0);

        double course = 45.0;

        double DIST_IN_KM = 157.2;
        double KM_PER_NM = 1.852;
        double distance = DIST_IN_KM / KM_PER_NM;

        LatLong128 actualDestination = source.projectOut(course, distance);
        LatLong128 expectedDestination = new LatLong128(1.0, 1.0);

        double TOLERANCE = 0.01;

        assertEquals(actualDestination.latitude(), expectedDestination.latitude(), TOLERANCE);
        assertEquals(actualDestination.longitude(), expectedDestination.longitude(), TOLERANCE);
    }

    @Test
    public void testProjectionInDegrees_negativeDist() {

        LatLong128 source = new LatLong128(0.0, 0.0);

        double course = 45.0;

        double DIST_IN_KM = 157.2;
        double KM_PER_NM = 1.852;
        double distance = DIST_IN_KM / KM_PER_NM;
        double negativeDistance = -distance;

        LatLong128 actualDestination = source.projectOut(course, negativeDistance);
        LatLong128 expectedDestination = new LatLong128(-1.0, -1.0);

        double TOLERANCE = 0.01;

        assertEquals(actualDestination.latitude(), expectedDestination.latitude(), TOLERANCE);
        assertEquals(actualDestination.longitude(), expectedDestination.longitude(), TOLERANCE);
    }

    @Test
    public void testGreatCircleOrigin() {

        LatLong128 source = new LatLong128(0.0, 0.0);
        Double course = 0.0; // traveling due north

        LatLong128 expected = new LatLong128(0.0, 90.0); // a point on the equator 1/4 around the earth
        LatLong128 actual = source.greatCircleOrigin(course);

        double TOLERANCE = 0.01; // some rounding is ok, this test is just for macro errors

        assertEquals(expected.latitude(), actual.latitude(), TOLERANCE);
        assertEquals(expected.longitude(), actual.longitude(), TOLERANCE);
    }

    @Test
    public void testMinMaxMethods() {

        LatLong128 v1 = LatLong128.of(40.75, -73.9);
        LatLong128 v2 = LatLong128.of(40.75, -74.1);
        LatLong128 v3 = LatLong128.of(40.7, -74.1);
        LatLong128 v4 = LatLong128.of(40.7, -73.9);

        List<LatLong128> points = newArrayList(v1, v2, v3, v4);

        double TOLERANCE = 0.001;
        assertEquals(40.7, minLatitude(points), TOLERANCE);
        assertEquals(40.75, maxLatitude(points), TOLERANCE);
        assertEquals(-74.1, minLongitude(points), TOLERANCE);
        assertEquals(-73.9, maxLongitude(points), TOLERANCE);
    }

    @Test
    public void testMinLatitude_nullInput() {
        // input cannot be null
        assertThrows(NullPointerException.class, () -> minLongitude(null));
    }

    @Test
    public void testMinLatitude_emptyInput() {
        // input cannot be empty
        assertThrows(IllegalArgumentException.class, () -> minLatitude(newArrayList()));
    }

    @Test
    public void testMaxLatitude_nullInput() {
        // input cannot be null
        assertThrows(NullPointerException.class, () -> maxLatitude(null));
    }

    @Test
    public void testMaxLatitude_emptyInput() {
        // input cannot be empty
        assertThrows(IllegalArgumentException.class, () -> maxLatitude(newArrayList()));
    }

    @Test
    public void testMinLongitude_nullInput() {
        // input cannot be null
        assertThrows(NullPointerException.class, () -> minLatitude(null));
    }

    @Test
    public void testMinLongitude_emptyInput() {
        // input cannot be empty
        assertThrows(IllegalArgumentException.class, () -> minLatitude(newArrayList()));
    }

    @Test
    public void testMaxLongitude_nullInput() {
        // input cannot be null
        assertThrows(NullPointerException.class, () -> maxLongitude(null));
    }

    @Test
    public void testMaxLongitude_emptyInput() {
        // input cannot be empty
        assertThrows(IllegalArgumentException.class, () -> maxLongitude(newArrayList()));
    }

    @Test
    public void testAvgLatLong() {
        LatLong128 one = LatLong128.of(0.0, 10.0);
        LatLong128 two = LatLong128.of(10.0, 20.0);
        LatLong128 naiveAverage = LatLong128.of(5.0, 15.0); // naive arthimatic average of LatLong
        LatLong128 correctAverage = avgLatLong(one, two);

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

        LatLong128 avg = avgLatLong(north, south);

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
        LatLong128 correctAverage = avgLatLong(east, west);

        Distance distBtwPoints = east.distanceTo(west);

        // the distance between the east and west points is small (about 90.01 NM)
        assertThat(distBtwPoints.isLessThan(Distance.ofNauticalMiles(91.0))).isTrue();
        assertThat(distBtwPoints.isGreaterThan(Distance.ofNauticalMiles(89.0))).isTrue();

        // the distance between the average point and the east point is about 45.5 NM
        assertThat(correctAverage.distanceTo(east).isLessThan(Distance.ofNauticalMiles(45.5))).isTrue();
        assertThat(correctAverage.distanceTo(east).isGreaterThan(Distance.ofNauticalMiles(44.5))).isTrue();

        // the distance between the average point and the west point is about 45.5 NM
        assertThat(correctAverage.distanceTo(west).isLessThan(Distance.ofNauticalMiles(45.5))).isTrue();
        assertThat(correctAverage.distanceTo(west).isGreaterThan(Distance.ofNauticalMiles(44.5))).isTrue();

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

        LatLong128 realAverage = avgLatLong(one, two, three);
        LatLong128 manualAverage = LatLong128.of(0.33, 179.5);

        // the real average is NOT the manual average
        assertThat(realAverage.distanceTo(manualAverage).isGreaterThan(Distance.ofNauticalMiles(0))).isTrue();

        // distance are small -- so error won't be too big assuming the internation date line doesn't hose the
        // computation
        assertThat(realAverage.latitude() > .33 && realAverage.latitude() < .34).isTrue();
        assertThat(realAverage.longitude() > 179.5 && realAverage.longitude() < 179.51).isTrue();
    }

    @Test
    public void testQuickAvgLatLong_1() {

        LatLong128 east = LatLong128.of(0.0, -179.5); // just east of line
        LatLong128 west = LatLong128.of(0.0, 179.0); // just west of line
        LatLong128 quickAverage = LatLong128.quickAvgLatLong(east, west);
        LatLong128 expected = LatLong128.of(0.0, 179.750);

        assertThat(quickAverage.distanceTo(expected).isLessThan(Distance.ofNauticalMiles(0.001))).isTrue();
    }

    @Test
    public void testQuickAvgLatLong_2() {

        LatLong128 east = LatLong128.of(-11.0, 12.5);
        LatLong128 west = LatLong128.of(15.0, -42.0);
        LatLong128 quickAverage = LatLong128.quickAvgLatLong(east, west);
        LatLong128 expected = LatLong128.of(2.0, -14.75);

        assertThat(quickAverage.distanceTo(expected).isLessThan(Distance.ofNauticalMiles(0.001))).isTrue();
    }

    @Test
    public void testAvgLatLong_similarResults_differentMethods() {

        LatLong128 one = LatLong128.of(42.93675, -83.70776);
        LatLong128 two = LatLong128.of(42.95037, -83.70570);

        // compute the solution two ways..
        LatLong128 quickAverage = LatLong128.quickAvgLatLong(one, two);
        LatLong128 accurateAverage = avgLatLong(one, two);

        // solutions a NOT the same...but they are damn near indistiquishable
        assertNotEquals(quickAverage, accurateAverage);
        assertThat(quickAverage.distanceTo(accurateAverage).isLessThan(Distance.ofNauticalMiles(0.00005))).isTrue();
    }

    @Test
    public void quickAvgLatLong_simple() {

        // These points are 846.45952 Nautical Miles apart!
        // The "naive average location" will be WRONG
        LatLong128 one = LatLong128.of(0.0, 10.0);
        LatLong128 two = LatLong128.of(10.0, 20.0);

        ArrayList<LatLong128> path = newArrayList(one, two);

        LatLong128 average = avgLatLong(path); // accurately computed avg LatLong
        LatLong128 naiveAverage = LatLong128.of(5.0, 15.0); // naive arithmetic average of LatLong

        assertThat(LatLong128.quickAvgLatLong(path)).isEqualTo(naiveAverage);

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
        LatLong128 correctAverage = LatLong128.quickAvgLatLong(path); // (0.0,179.75)

        Distance distBtwPoints = east.distanceTo(west);

        // the distance between the east and west points is small (about 90.01 NM)
        assertThat(distBtwPoints.isLessThan(Distance.ofNauticalMiles(91.0))).isTrue();
        assertThat(distBtwPoints.isGreaterThan(Distance.ofNauticalMiles(89.0))).isTrue();

        // the distance between the average point and the east point is about 45.5 NM
        assertThat(correctAverage.distanceTo(east).isLessThan(Distance.ofNauticalMiles(45.5))).isTrue();
        assertThat(correctAverage.distanceTo(east).isGreaterThan(Distance.ofNauticalMiles(44.5))).isTrue();

        // the distance between the average point and the west point is about 45.5 NM
        assertThat(correctAverage.distanceTo(west).isLessThan(Distance.ofNauticalMiles(45.5))).isTrue();
        assertThat(correctAverage.distanceTo(west).isGreaterThan(Distance.ofNauticalMiles(44.5))).isTrue();

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

        LatLong128 east = LatLong128.of(0.0, -179.5); // just east of line  (aka "180 - .5")
        LatLong128 west_1 = LatLong128.of(0.0, 179.0); // just west of line (aka "180 + 1")
        LatLong128 west_2 = LatLong128.of(0.0, 179.0); // just west of line (aka "180 + 1")

        ArrayList<LatLong128> path_1 = newArrayList(east, west_1);
        ArrayList<LatLong128> path_2 = newArrayList(east, west_1, west_2);

        LatLong128 midPoint = LatLong128.quickAvgLatLong(path_1); // should be midpoint
        LatLong128 twoThirdPoint = LatLong128.quickAvgLatLong(path_2); // should be 2/3rds towards west

        assertThat(midPoint).isEqualTo(LatLong128.of(0.0, 179.75));
        assertThat(twoThirdPoint).isEqualTo(LatLong128.of(0.0, 179.5));

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
        ArrayList<LatLong128> path =
                newArrayList(LatLong128.of(0.0, 0.1), LatLong128.of(1.0, 1.1), LatLong128.of(2.0, 2.1), LatLong128.of(3.0, 3.1));

        // compute the solution two ways ..
        LatLong128 quickAverage = LatLong128.quickAvgLatLong(path);
        LatLong128 accurateAverage = avgLatLong(path);

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

        assertThrows(NoSuchElementException.class, () -> LatLong128.quickAvgLatLong(locations));
    }

    @Test
    public void avgLatLong_empty_arrays() {
        /*
         * No change in behavior between avgLatLong and quickAvgLatLong
         */

        LatLong128[] locations = new LatLong128[0];

        assertThrows(NoSuchElementException.class, () -> avgLatLong(locations));

        assertThrows(NoSuchElementException.class, () -> LatLong128.quickAvgLatLong(locations));
    }

    @Test
    public void avgLatLong_null_collection() {
        /*
         * No change in behavior between avgLatLong and quickAvgLatLong
         */

        ArrayList<LatLong128> locations = null;

        assertThrows(NullPointerException.class, () -> avgLatLong(locations));

        assertThrows(NullPointerException.class, () -> LatLong128.quickAvgLatLong(locations));
    }

    @Test
    public void avgLatLong_null_arrays() {
        /*
         * No change in behavior between avgLatLong and quickAvgLatLong
         */

        LatLong128[] locations = null;

        assertThrows(NullPointerException.class, () -> avgLatLong(locations));

        assertThrows(NullPointerException.class, () -> LatLong128.quickAvgLatLong(locations));
    }

    @Test
    public void isWithin_samePoint() {

        LatLong128 one = LatLong128.of(0.0, 0.0);
        LatLong128 two = LatLong128.of(0.0, 0.0); // same point twice

        assertThat(one.isWithin(Distance.ofFeet(0), two)).isTrue();
        assertThat(one.isWithin(Distance.ofFeet(0.1), two)).isTrue();
        assertThat(one.isWithin(Distance.ofFeet(-1.0), two)).isFalse();
    }

    @Test
    public void isWithin_differentPoints() {

        LatLong128 one = new LatLong128(0.0, 0.0);
        LatLong128 two = new LatLong128(1.0, 1.0);

        Distance computedDistance = one.distanceTo(two);

        double EXPECTED_DIST_IN_KM = 157.2;

        Distance expectedDist = Distance.ofKiloMeters(EXPECTED_DIST_IN_KM);

        double TOLERANCE = 0.05;
        assertThat(computedDistance.minus(expectedDist).inKilometers()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(one.isWithin(computedDistance, two)).isTrue();
    }

    @Test
    void can_compress() {

        LatLong128 raw = new LatLong128(83.225689134, -22.324187543);
        LatLong compressed = raw.compress();

        assertThat(raw.latitude()).isCloseTo(compressed.latitude(), within(0.0000001));
        assertThat(raw.longitude()).isCloseTo(compressed.longitude(), within(0.0000001));
    }

    @Test
    public void canBuildUsingPrivateNoArgConstructor() {

        Class<LatLong128> clazz = LatLong128.class;
        Class<?>[] NO_ARG = new Class[]{};

        // We can build a LatLong object using a private no-arg constructor
        assertDoesNotThrow(() -> {
            Constructor<LatLong128> structor = clazz.getDeclaredConstructor(NO_ARG);
            structor.setAccessible(true); // don't allow a private constructor to stop us!

            LatLong128 location = structor.newInstance((Object[]) NO_ARG);
        });
    }

    @Test
    public void isAvroCompatible() throws IOException {

        Schema schema = ReflectData.get().getSchema(LatLong128.class);
        DataFileWriter<LatLong128> dfw = new DataFileWriter<>(new ReflectDatumWriter<>(schema));

        File targetFile = new File(tempDir, "LatLong128.avro");
        assertThat(targetFile.exists()).isFalse();

        dfw.create(schema, targetFile);

        dfw.append(LatLong128.of(0.0, 0.0));
        dfw.append(LatLong128.of(5.0, -5.0));
        dfw.close();

        DataFileReader<LatLong128> reader = new DataFileReader<>(targetFile, new ReflectDatumReader<>(schema));

        LatLong128 first = reader.next();
        LatLong128 second = reader.next();

        assertThat(first).isEqualTo(LatLong128.of(0.0, 0.0));
        assertThat(second).isEqualTo(LatLong128.of(5.0, -5.0));
        assertThat(reader.hasNext()).isFalse();

        reader.close();
    }
}

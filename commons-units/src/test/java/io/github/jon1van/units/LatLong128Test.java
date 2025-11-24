package io.github.jon1van.units;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.units.HasLatLong.*;
import static io.github.jon1van.units.LatLong128.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

        assertThat(a.equals(b)).isTrue(); // they ARE equal
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

        HasLatLong actualDestination = source.move(course, distance);
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

        HasLatLong actualDestination = source.move(course, negativeDistance);
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
}

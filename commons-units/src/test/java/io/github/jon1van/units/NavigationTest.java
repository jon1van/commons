package io.github.jon1van.units;

import static io.github.jon1van.units.HasLatLong.from;
import static io.github.jon1van.units.Navigation.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NavigationTest {

    @Test
    public void testOneQuarterCircumferenceOfEarthInNM() {

        double EARTH_RADIUS_IN_MILES = 3_959; // according to google
        double MILES_PER_NAUTICAL_MILE = 1.15078; // according to google
        double EARTH_RADIUS_IN_NM = EARTH_RADIUS_IN_MILES / MILES_PER_NAUTICAL_MILE;
        double CIRCUMFERENCE_OF_EARTH_IN_NM = 2.0 * Math.PI * EARTH_RADIUS_IN_NM;

        double expected = CIRCUMFERENCE_OF_EARTH_IN_NM / 4;

        assertThat(oneQuarterCircumferenceOfEarthInNM()).isCloseTo(expected, within(5.0));
    }

    @Test
    public void testFeetPerNM() {

        double FEET_PER_NM = 6076.12; // according to google

        assertThat(Navigation.feetPerNM()).isCloseTo(FEET_PER_NM, within(0.05));
    }

    @Test
    public void testDistanceInNM_4args() {

        // according to http://www.movable-type.co.uk/scripts/latlong.html
        // The distance btw (0.0, 0.0) and (10.0, 10.0) is 1569 km
        double KM_PER_NM = 1.852;
        double expected = 1569 / KM_PER_NM;

        assertThat(Navigation.distanceInNM(0.0, 0.0, 10.0, 10.0)).isCloseTo(expected, within(0.75));
    }

    @Test
    public void testCourseInDegrees_sameLatLongTwice() {

        LatLong locationA = LatLong.of(42.0, 21.0);
        LatLong locationB = LatLong.of(21.0, 42.0);

        double expected = 360.0;
        double TOLERANCE = 0.001;

        assertThat(courseInDegrees(locationA, locationA)).isCloseTo(expected, within(TOLERANCE));
        assertThat(courseInDegrees(locationB, locationB)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    public void testCourseInDegrees_4args() {
        assertThat(courseInDegrees(0.0, 0.0, 0.0, 10.0)).isCloseTo(90.0, within(0.001)); // East
        assertThat(courseInDegrees(0.0, 10.0, 0.0, 0.0)).isCloseTo(270.0, within(0.001)); // West
        assertThat(courseInDegrees(0.0, 0.0, 10.0, 0.0)).isCloseTo(360.0, within(0.001)); // North
        assertThat(courseInDegrees(10.0, 0.0, 00.0, 0.0)).isCloseTo(180.0, within(0.001)); // South
        assertThat(courseInDegrees(0.0, 0.0, 1.0, 1.0)).isCloseTo(45.0, within(0.01)); // NE
    }

    @Test
    public void courseBtw_hasLatLong() {

        HasLatLong a = from(0.0, 0.0); // start at 0.0, 0.0
        HasLatLong b = from(1.0, 1.0); // move toward 1.0, 1.0

        // you travel at almost 45 degrees
        assertThat(courseBtw(a, b).inDegrees()).isCloseTo(45.0, within(0.005));
    }

    @Test
    public void testProjectionInDegrees_4args() {

        // get Direction and Distance from (0.0, 0.0) to (10.0, 10.0)
        double headingInDegrees = Navigation.courseInDegrees(0.0, 0.0, 10.0, 10.0);
        double distNM = Navigation.distanceInNM(0.0, 0.0, 10.0, 10.0);

        LatLong128 actual = Navigation.move(0.0, 0.0, headingInDegrees, distNM);
        LatLong expected = new LatLong(10.0, 10.0);

        assertThat(actual.latitude()).isEqualTo(expected.latitude(), within(0.001));
        assertThat(actual.longitude()).isEqualTo(expected.longitude(), within(0.001));
    }

    @Test
    public void testGreatCircleOrigin() {

        Double latitude = 0.0;
        Double longitude = 0.0;
        Double course = 90.0; // traveling due east

        LatLong128 expected = new LatLong128(-90.0, 0.0); // the south pole
        LatLong128 actual = Navigation.greatCircleOrigin(latitude, longitude, course);

        double TOLERANCE = 0.01; // some rounding is ok, this test is just for macro errors

        assertEquals(expected.latitude(), actual.latitude(), TOLERANCE);
        // all longitudes work at a pole
    }

    @Test
    public void testGreatCircleOrigin2() {

        Double latitude = 0.0;
        Double longitude = 0.0;
        Double course = 270.0; // traveling due west

        LatLong128 expected = new LatLong128(90.0, 0.0); // the north pole
        LatLong128 actual = Navigation.greatCircleOrigin(latitude, longitude, course);

        double TOLERANCE = 0.001; // some rounding is ok, this test is just for macro errors

        assertThat(actual.latitude()).isEqualTo(expected.latitude(), within(TOLERANCE));
        // all longitudes work at a pole
        assertThat(actual.latitude()).isEqualTo(expected.latitude(), within(TOLERANCE));
    }

    @Test
    public void testGreatCircleOrigin3() {

        Double latitude = 0.0;
        Double longitude = 0.0;
        Double course = 0.0; // traveling due north

        LatLong128 expected = new LatLong128(0.0, 90.0); // a point on the equator 1/4 around the earth
        LatLong128 actual = Navigation.greatCircleOrigin(latitude, longitude, course);

        double TOLERANCE = 0.01; // some rounding is ok, this test is just for macro errors

        assertEquals(expected.latitude(), actual.latitude(), TOLERANCE);
        assertEquals(expected.longitude(), actual.longitude(), TOLERANCE);
    }

    @Test
    public void testGreatCircleOrigin4() {

        Double latitude = 0.0;
        Double longitude = 0.0;
        Double course = 180.0; // traveling due south

        LatLong128 expected = new LatLong128(0.0, -90.0); // a point on the equator 1/4 around the earth
        LatLong128 actual = Navigation.greatCircleOrigin(latitude, longitude, course);

        double TOLERANCE = 0.01; // some rounding is ok, this test is just for macro errors

        assertEquals(expected.latitude(), actual.latitude(), TOLERANCE);
        assertEquals(expected.longitude(), actual.longitude(), TOLERANCE);
    }

    @Test
    public void testDistanceInNM_LatLongPair_LatLongPair() {

        LatLong a = LatLong.of(0.0, 0.0);
        LatLong b = LatLong.of(10.0, 10.0);

        // according to http://www.movable-type.co.uk/scripts/latlong.html
        // The distance btw (0.0, 0.0) and (10.0, 10.0) is 1569 km
        double KM_PER_NM = 1.852;
        double expected = 1569 / KM_PER_NM;

        assertThat(Navigation.distanceInNM(a, b)).isEqualTo(expected, within(0.75));
    }

    @Test
    public void testCourseInDegrees_LatLongPair_LatLongPair() {

        LatLong a = LatLong.of(0.0, 0.0);
        LatLong b = LatLong.of(1.0, 1.0);

        assertThat(courseInDegrees(a, b)).isEqualTo(45.0, within(0.005));
    }

    @Test
    public void testAngleDifference_Double() {
        double TOLERANCE = 0.0001;

        // test positive inputs
        assertThat(angleDifference(5.0)).isEqualTo(5.0, within(TOLERANCE));
        assertThat(angleDifference(175.0)).isEqualTo(175.0, within(TOLERANCE));
        assertThat(angleDifference(185.0)).isEqualTo(-175.0, within(TOLERANCE));
        assertThat(angleDifference(355.0)).isEqualTo(-5.0, within(TOLERANCE));

        // test negative inputs
        assertThat(angleDifference(-5.0)).isEqualTo(-5.0, within(TOLERANCE));
        assertThat(angleDifference(-175.0)).isEqualTo(-175.0, within(TOLERANCE));
        assertThat(angleDifference(-185.0)).isEqualTo(175.0, within(TOLERANCE));
    }

    @Test
    public void testAngleDifference_2args() {

        double epsilon = 0.0001;

        assertThat(angleDifference(5.0, 355.0)).isEqualTo(10.0, within(epsilon));
        assertThat(angleDifference(355.0, 5.0)).isEqualTo(-10.0, within(epsilon));
    }

    //    @Test
    //    public void testCrossTrackDistance() {
    //
    //        double TOLERANCE = 0.0001;
    //
    //        HasPosition ls = () -> LatLong.of(0.0, 0.0);
    //        HasPosition le = () -> LatLong.of(0.0, 10.0);
    //        HasPosition p = () -> LatLong.of(1.0, 0.5);
    //
    //        double CTD = crossTrackDistanceNM(ls, le, p);
    //        double ATD = alongTrackDistanceNM(ls, le, p);
    //
    //        assertTrue(CTD < 0.0);
    //        assertTrue(ATD < (-1.0 * CTD));
    //
    //        assertEquals(-60.00686673640662, CTD, TOLERANCE);
    //        assertEquals(30.00343415285915, ATD, TOLERANCE);
    //
    //        p = () -> LatLong.of(1.0, -0.5);
    //
    //        CTD = crossTrackDistanceNM(ls, le, p);
    //        ATD = alongTrackDistanceNM(ls, le, p, CTD);
    //        assertEquals(-30.00343415285915, ATD, TOLERANCE);
    //
    //        p = () -> LatLong.of(1.0, 11.0);
    //
    //        CTD = crossTrackDistanceNM(ls, le, p);
    //        ATD = alongTrackDistanceNM(ls, le, p, CTD);
    //        assertTrue(ATD > ls.distanceInNmTo(le));
    //    }
    //
    //    @Test
    //    void testAlongTrackDistanceFloatingPointError() {
    //        // In the past there 3 points generated a NaN for the alongTrackDistance computation
    //        // These 3 point form a Triangle with sides:
    //        // start-end = 25.97489NM
    //        // start-point = 0.01393NM
    //        // end-point = 25.97490NM
    //        final HasPosition START = HasPosition.from(46.294875, -119.96004166666667);
    //        final HasPosition END = HasPosition.from(46.57024166666667, -120.44463611111111);
    //        final HasPosition POINT = HasPosition.from(46.29469627061987, -119.96025624188381);
    //
    //        double atd_method1 = alongTrackDistanceNM(START, END, POINT);
    //        double atd_method2 = alongTrackDistanceNM(START, END, POINT, crossTrackDistanceNM(START, END, POINT));
    //
    //        assertThat(atd_method1, is(not(Double.NaN)));
    //        assertThat(atd_method2, is(not(Double.NaN)));
    //        assertThat(atd_method1, is(atd_method2));
    //    }
    //
    //    @Disabled
    //    @Test
    //    void testShowMapForAlongTrackDistanceFloatingPointError() {
    //        // In the past there 3 points generated a NaN for the alongTrackDistance computation
    //        // These 3 point form a Triangle with sides:
    //        // start-end = 25.97489NM
    //        // start-point = 0.01393NM
    //        // end-point = 25.97490NM
    //        final HasPosition START = HasPosition.from(46.294875, -119.96004166666667);
    //        final HasPosition END = HasPosition.from(46.57024166666667, -120.44463611111111);
    //        final HasPosition POINT = HasPosition.from(46.29469627061987, -119.96025624188381);
    //
    //        MapBuilder.newMapBuilder()
    //                .center(LatLong.avgLatLong(START.latLong(), END.latLong()))
    //                .width(Distance.ofNauticalMiles(30))
    //                .mapBoxDarkMode()
    //                .addFeature(MapFeatures.line(START.latLong(), END.latLong(), Color.RED, 2.0f))
    //                .addFeature(MapFeatures.circle(POINT.latLong(), Color.GREEN, 15, 2.0f))
    //                .toFile(new File("alongTrackNumericError.png"));
    //    }

    @Test
    void testArcLengthSmallRadius() {
        double radius = 1.0;
        double angle = 90;

        assertThat(Navigation.arcLength(radius, angle)).isEqualTo(Math.PI / 2.0, within(0.01));
    }

    @Test
    void testArcLengthLargeRadius() {
        double radius = oneQuarterCircumferenceOfEarthInNM();
        double angle = 90;

        assertThat(oneQuarterCircumferenceOfEarthInNM()).isEqualTo(Navigation.arcLength(radius, angle), within(0.01));
    }
}

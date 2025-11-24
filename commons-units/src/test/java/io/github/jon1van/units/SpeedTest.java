package io.github.jon1van.units;

import static io.github.jon1van.units.Distance.Unit.METERS;
import static io.github.jon1van.units.Distance.Unit.MILES;
import static io.github.jon1van.units.Speed.Unit.*;
import static io.github.jon1van.units.Speed.unitFromString;
import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

public class SpeedTest {

    @Test
    public void testSpeedZeroTimeDelta() {
        // timeDelta must be positive
        assertThrows(IllegalArgumentException.class, () -> new Speed(Distance.ofNauticalMiles(1), Duration.ZERO));
    }

    @Test
    public void testSpeedNegativeTimeDelta() {
        // timeDelta must be positive
        assertThrows(
                IllegalArgumentException.class,
                () -> new Speed(
                        Distance.ofNauticalMiles(1), Duration.ofMinutes(1).negated()));
    }

    @Test
    public void testInKnots() {
        Speed speed = new Speed(Distance.ofNauticalMiles(1), Duration.ofHours(1));

        assertEquals(1.0, speed.inKnots(), 0.00001);
    }

    @Test
    public void testInKnots_2() {
        Speed speed = new Speed(Distance.ofNauticalMiles(20), Duration.ofHours(1));

        assertEquals(20.0, speed.inKnots(), 0.00001);
    }

    @Test
    public void testInKnots_3() {
        Speed speed = new Speed(Distance.ofNauticalMiles(20), Duration.ofMinutes(10));

        assertEquals(120.0, speed.inKnots(), 0.00001);
    }

    @Test
    public void testConversion() {
        // 20 knots
        Speed speed = new Speed(Distance.ofNauticalMiles(20), Duration.ofHours(1));

        double TOL = 0.00001;
        assertEquals(20.0, speed.inKnots(), 20.0 * TOL);
        assertEquals(37.04, speed.inKilometersPerHour(), 37.04 * TOL);
        assertEquals(10.2889, speed.inMetersPerSecond(), 10.2889 * TOL);
        assertEquals(23.0156, speed.inMilesPerHour(), 23.0156 * TOL);
    }

    @Test
    public void testLiterateConstruction() {
        Speed oneKnot = Speed.of(1, KNOTS);

        double TOL = 0.00001;
        assertEquals(1.0, oneKnot.inKnots(), TOL);
        assertEquals(1.15078, oneKnot.inMilesPerHour(), 1.15078 * TOL);
    }

    @Test
    public void testBothConstructionMethodsAgree() {
        Speed oneKnot = Speed.of(1, KNOTS);
        Speed oneKnotByDef = new Speed(Distance.ofNauticalMiles(1.0), Duration.ofHours(1));

        double TOL = 0.00001;
        assertEquals(oneKnot.inKnots(), oneKnotByDef.inKnots(), TOL);
        assertEquals(oneKnot.inMetersPerSecond(), oneKnotByDef.inMetersPerSecond(), TOL);
    }

    @Test
    public void testBetween() {
        LatLong pos1 = LatLong.of(0.0, 0.0);
        Instant time1 = EPOCH;

        LatLong pos2 = LatLong.of(1.0, 1.0);
        Instant time2 = EPOCH.plus(Duration.ofHours(1));

        Distance dist = pos1.distanceTo(pos2);
        Duration timeDelta = Duration.ofHours(1);

        Speed manuallyBuiltSpeed = new Speed(dist, timeDelta);
        Speed autoSpeed = Speed.between(pos1, time1, pos2, time2);

        double TOL = 0.0001;
        assertEquals(manuallyBuiltSpeed.inKnots(), autoSpeed.inKnots(), TOL);
    }

    @Test
    public void equalsReducesUnits() {
        Speed oneMeterPerSecond = new Speed(Distance.of(1, METERS), Duration.ofSeconds(1));
        Speed oneMeterPerSecond2 = new Speed(Distance.of(20, METERS), Duration.ofSeconds(20));
        Speed oneMilePerSecond = new Speed(Distance.of(1, MILES), Duration.ofSeconds(1));

        assertEquals(oneMeterPerSecond, oneMeterPerSecond2);
        assertEquals(oneMeterPerSecond.hashCode(), oneMeterPerSecond2.hashCode());

        assertNotEquals(oneMeterPerSecond, "hello");
        assertNotEquals(oneMeterPerSecond, oneMilePerSecond);
        assertNotEquals(null, oneMilePerSecond);
    }

    @Test
    public void testTimes_Distance() {

        Speed oneKnot = Speed.of(1.0, KNOTS);

        assertThat(oneKnot.times(Duration.ZERO)).isEqualTo(Distance.ofNauticalMiles(0));

        assertThat(oneKnot.times(Duration.ofMinutes(30))).isEqualTo(Distance.ofNauticalMiles(0.5));
        assertThat(oneKnot.times(Duration.ofMinutes(60))).isEqualTo(Distance.ofNauticalMiles(1));
    }

    @Test
    public void times_scalar() {

        Speed oneKnot = Speed.of(1.0, KNOTS);

        assertThat(oneKnot.times(-5)).isEqualTo(Speed.of(-5.0, KNOTS));
        assertThat(oneKnot.times(0)).isEqualTo(Speed.of(0.0, KNOTS));
        assertThat(oneKnot.times(11.1)).isEqualTo(Speed.of(11.1, KNOTS));
    }

    @Test
    public void plusAnotherSpeed() {

        Speed oneKnot = Speed.of(1.0, KNOTS);
        Speed threeFps = Speed.of(3.0, FEET_PER_SECOND);
        Speed fiveMph = Speed.of(5.0, MILES_PER_HOUR);
        Speed sevenKph = Speed.of(7.0, KILOMETERS_PER_HOUR);
        Speed elevenMps = Speed.of(11.0, METERS_PER_SECOND);

        double epsilon = 0.0005;

        // 3fps = 1.7774514038876894 knots
        assertThat(oneKnot.plus(threeFps).inKnots()).isEqualTo(2.7774514038876896, within(epsilon));
        assertThat(threeFps.plus(oneKnot).inKnots()).isEqualTo(2.7774514038876896, within(epsilon));

        // 3fps = 2.0454545454545454 mph
        assertThat(fiveMph.plus(threeFps).inMilesPerHour()).isEqualTo(7.045454545454546, within(epsilon));
        assertThat(threeFps.plus(fiveMph).inMilesPerHour()).isEqualTo(7.045454545454546, within(epsilon));

        // 5pmh = 8.04672 kph
        assertThat(fiveMph.plus(sevenKph).inKilometersPerHour()).isEqualTo(15.046720000000002, within(epsilon));
        assertThat(sevenKph.plus(fiveMph).inKilometersPerHour()).isEqualTo(15.046720000000002, within(epsilon));

        // 7kph = 1.9444444444444444 mps
        assertThat(elevenMps.plus(sevenKph).inMetersPerSecond()).isEqualTo(12.944444444444445, within(epsilon));
        assertThat(sevenKph.plus(elevenMps).inMetersPerSecond()).isEqualTo(12.944444444444445, within(epsilon));
    }

    @Test
    public void minusAnotherSpeed() {

        Speed oneKnot = Speed.of(1.0, KNOTS);
        Speed threeFps = Speed.of(3.0, FEET_PER_SECOND);
        Speed fiveMph = Speed.of(5.0, MILES_PER_HOUR);
        Speed sevenKph = Speed.of(7.0, KILOMETERS_PER_HOUR);
        Speed elevenMps = Speed.of(11.0, METERS_PER_SECOND);

        double epsilon = 0.0005;

        // 3fps = 1.7774514038876894 knots
        assertThat(oneKnot.minus(threeFps).inKnots()).isEqualTo(-0.777451403887689, within(epsilon));
        assertThat(threeFps.minus(oneKnot).inKnots()).isEqualTo(0.777451403887689, within(epsilon));

        // 3fps = 2.0454545454545454 mph
        assertThat(fiveMph.minus(threeFps).inMilesPerHour()).isEqualTo(2.9545454545454546, within(epsilon));
        assertThat(threeFps.minus(fiveMph).inMilesPerHour()).isEqualTo(-2.954545454545455, within(epsilon));

        // 5pmh = 8.04672 kph
        assertThat(fiveMph.minus(sevenKph).inKilometersPerHour()).isEqualTo(1.0467199999999997, within(epsilon));
        assertThat(sevenKph.minus(fiveMph).inKilometersPerHour()).isEqualTo(-1.0467199999999997, within(epsilon));

        // 7kph = 1.9444444444444444 mps
        assertThat(elevenMps.minus(sevenKph).inMetersPerSecond()).isEqualTo(9.055555555555555, within(epsilon));
        assertThat(sevenKph.minus(elevenMps).inMetersPerSecond()).isEqualTo(-9.055555555555555, within(epsilon));
    }

    @Test
    public void testDistanceCoveredIn_negativeDuration() {
        Speed oneKnot = Speed.of(1.0, KNOTS);

        // negative duration is forbidden
        assertThrows(IllegalArgumentException.class, () -> oneKnot.times(Duration.ofHours(-1)));
    }

    @Test
    public void testTimeToTravel() {

        Speed oneKnot = Speed.of(1.0, KNOTS);

        // It takes 1 hour to travel 1 NM at 1 knot
        assertThat(oneKnot.timeToTravel(Distance.ofNauticalMiles(1))).isEqualTo(Duration.ofHours(1));

        // It takes 2 hours to travel 2 NM at 1 knot
        assertThat(oneKnot.timeToTravel(Distance.ofNauticalMiles(2))).isEqualTo(Duration.ofHours(2));

        // It takes 30 minutes to go 1 NM at 2 knots
        assertThat(Speed.of(2.0, KNOTS).timeToTravel(Distance.ofNauticalMiles(1)))
                .isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    public void testFeetPerSecondUnit() {

        // 20 feet per second
        Speed speed = new Speed(Distance.ofFeet(20), Duration.ofSeconds(1));

        Duration travelTime = speed.timeToTravel(Distance.ofFeet(20));

        assertThat(travelTime).isEqualTo(Duration.ofSeconds(1));

        // 20 fps = 6.096 mps = 20 ft * 0.3048 meters-per-foot
        assertEquals(6.096, speed.inMetersPerSecond(), 0.0000001);
        // convert ft to nm and seconds to hours
        assertEquals(20.0 / Navigation.feetPerNM() * 60.0 * 60.0, speed.inKnots(), 0.0000001);
    }

    @Test
    public void testIsPositive() {
        Speed negativeOne = Speed.of(-1, KNOTS);
        assertFalse(negativeOne.isPositive());

        Speed zero = Speed.of(0.0, KNOTS);
        assertFalse(zero.isPositive());

        Speed one = Speed.of(1.0, KNOTS);
        assertTrue(one.isPositive());
    }

    @Test
    public void testIsNegative() {
        Speed negativeOne = Speed.of(-1, KNOTS);
        assertTrue(negativeOne.isNegative());

        Speed zero = Speed.of(0.0, KNOTS);
        assertFalse(zero.isNegative());

        Speed one = Speed.of(1.0, KNOTS);
        assertFalse(one.isNegative());
    }

    @Test
    public void testIsZero() {
        Speed negativeOne = Speed.of(-1, KNOTS);
        assertFalse(negativeOne.isZero());

        Speed zero = Speed.of(0.0, KNOTS);
        assertTrue(zero.isZero());

        Speed one = Speed.of(1.0, KNOTS);
        assertFalse(one.isZero());
    }

    @Test
    public void testComparisonMethods() {

        Speed halfMeterPerSec = Speed.of(0.5, METERS_PER_SECOND);
        Speed oneMeterPerSec = Speed.of(1.0, METERS_PER_SECOND);
        Speed oneThousandMetersPerSec = Speed.of(1_000, METERS_PER_SECOND);
        Speed oneKiloMeterPerSec = Speed.of(3_600, KILOMETERS_PER_HOUR);

        assertTrue(halfMeterPerSec.isLessThan(oneMeterPerSec));
        assertTrue(halfMeterPerSec.isLessThanOrEqualTo(oneMeterPerSec));
        assertTrue(oneMeterPerSec.isGreaterThan(halfMeterPerSec));
        assertTrue(oneMeterPerSec.isGreaterThanOrEqualTo(halfMeterPerSec));

        assertTrue(oneKiloMeterPerSec.isGreaterThanOrEqualTo(oneThousandMetersPerSec));
        assertTrue(oneKiloMeterPerSec.isLessThanOrEqualTo(oneThousandMetersPerSec));
    }

    @Test
    public void feetPerMinuteUnitAddedCorrectly() {

        // same speed -- two ways
        Speed sixtyFpm = Speed.of(60, FEET_PER_MINUTE);
        Speed oneFps = Speed.of(1, FEET_PER_SECOND);

        assertThat(sixtyFpm).isEqualTo(oneFps);

        Duration oneHour = Duration.ofHours(1);

        // You should go the same distance because you are going the same speed
        assertThat(sixtyFpm.times(oneHour)).isEqualTo(oneFps.times(oneHour));

        Distance oneNauticalMile = Distance.ofNauticalMiles(1);

        // It takes the same amount of time to travel 1 NM
        assertThat(sixtyFpm.timeToTravel(oneNauticalMile)).isEqualTo(oneFps.timeToTravel(oneNauticalMile));

        assertThat(sixtyFpm.inFeetPerMinutes()).isEqualTo(60.0);
        assertThat(sixtyFpm.inFeetPerSecond()).isEqualTo(1.0);
    }

    @Test
    public void unitFromStringProperlyParsesUnits() {

        assertThat(unitFromString("fpm")).isEqualTo(Speed.Unit.FEET_PER_MINUTE);
        assertThat(unitFromString("5.0fpm")).isEqualTo(Speed.Unit.FEET_PER_MINUTE);

        assertThat(unitFromString("fps")).isEqualTo(Speed.Unit.FEET_PER_SECOND);
        assertThat(unitFromString("5.0fps")).isEqualTo(Speed.Unit.FEET_PER_SECOND);

        assertThat(unitFromString("kph")).isEqualTo(Speed.Unit.KILOMETERS_PER_HOUR);
        assertThat(unitFromString("5.0kph")).isEqualTo(Speed.Unit.KILOMETERS_PER_HOUR);

        assertThat(unitFromString("kn")).isEqualTo(KNOTS);
        assertThat(unitFromString("5.0kn")).isEqualTo(KNOTS);

        assertThat(unitFromString("mps")).isEqualTo(METERS_PER_SECOND);
        assertThat(unitFromString("5.0mps")).isEqualTo(METERS_PER_SECOND);

        assertThat(unitFromString("mph")).isEqualTo(MILES_PER_HOUR);
        assertThat(unitFromString("5.0mph")).isEqualTo(MILES_PER_HOUR);

        assertThat(unitFromString("")).isNull();
        assertThat(unitFromString("notAUnit")).isNull();
    }

    @Test
    public void speedFromString_noUnitFound() {
        assertThrows(IllegalArgumentException.class, () -> Speed.fromString("notAUnit"));
    }

    @Test
    public void speedFromString_nullInput() {
        assertThrows(NullPointerException.class, () -> Speed.fromString(null));
    }

    @Test
    public void speedFromString() {

        // can parse when there is no space
        assertThat(Speed.fromString("5.0fpm")).isEqualTo(Speed.of(5, FEET_PER_MINUTE));
        assertThat(Speed.fromString("5.fps")).isEqualTo(Speed.of(5, FEET_PER_SECOND));
        assertThat(Speed.fromString("5.0kph")).isEqualTo(Speed.of(5, KILOMETERS_PER_HOUR));
        assertThat(Speed.fromString("5.0kn")).isEqualTo(Speed.of(5, KNOTS));
        assertThat(Speed.fromString("5.0mps")).isEqualTo(Speed.of(5, METERS_PER_SECOND));
        assertThat(Speed.fromString("5.0mph")).isEqualTo(Speed.of(5, MILES_PER_HOUR));

        // can parse when there is a space
        assertThat(Speed.fromString("5.0 fpm")).isEqualTo(Speed.of(5, FEET_PER_MINUTE));
        assertThat(Speed.fromString("5.0 fps")).isEqualTo(Speed.of(5, FEET_PER_SECOND));
        assertThat(Speed.fromString("5.0 kph")).isEqualTo(Speed.of(5, KILOMETERS_PER_HOUR));
        assertThat(Speed.fromString("5.0 kn")).isEqualTo(Speed.of(5, KNOTS));
        assertThat(Speed.fromString("5.0 mps")).isEqualTo(Speed.of(5, METERS_PER_SECOND));
        assertThat(Speed.fromString("5.0 mph")).isEqualTo(Speed.of(5, MILES_PER_HOUR));

        // can parse different levels of accuracy
        assertThat(Speed.fromString("5 fpm")).isEqualTo(Speed.of(5, FEET_PER_MINUTE));
        assertThat(Speed.fromString("5.01 fps")).isEqualTo(Speed.of(5.01, FEET_PER_SECOND));
        assertThat(Speed.fromString("5.12345 kph")).isEqualTo(Speed.of(5.12345, KILOMETERS_PER_HOUR));
        assertThat(Speed.fromString("5E5 kn")).isEqualTo(Speed.of(5E5, KNOTS));
        assertThat(Speed.fromString("5.000001 mph")).isEqualTo(Speed.of(5.000001, MILES_PER_HOUR));

        // can parse zero and negative numbers
        assertThat(Speed.fromString("-5 fpm")).isEqualTo(Speed.of(-5, FEET_PER_MINUTE));
        assertThat(Speed.fromString("-5.01 fps")).isEqualTo(Speed.of(-5.01, FEET_PER_SECOND));
        assertThat(Speed.fromString("-5.12345 kph")).isEqualTo(Speed.of(-5.12345, KILOMETERS_PER_HOUR));
        assertThat(Speed.fromString("-5E5 kn")).isEqualTo(Speed.of(-5E5, KNOTS));
        assertThat(Speed.fromString("0.0 mps")).isEqualTo(Speed.of(0, METERS_PER_SECOND));
        assertThat(Speed.fromString("-5.000001 mph")).isEqualTo(Speed.of(-5.000001, MILES_PER_HOUR));
    }

    @Test
    public void testToString_numDigits_speedUnit() {
        Speed oneKnot = Speed.of(1, KNOTS);
        assertThat("1.000kn").isEqualTo(oneKnot.toString());
        assertThat("1.0kn").isEqualTo(oneKnot.toString(1, KNOTS));
        assertThat("1.151mph").isEqualTo(oneKnot.toString(3, MILES_PER_HOUR));
        assertThat("0.51mps").isEqualTo(oneKnot.toString(2, METERS_PER_SECOND));
    }

    @Test
    public void toStringShouldReflectDeclaredUnit() {

        Speed oneKnot = Speed.of(1.0, KNOTS);
        Speed oneMeterPerSecond = Speed.of(1.0, METERS_PER_SECOND);
        Speed oneFootPerMinute = Speed.of(1.0, FEET_PER_MINUTE);
        Speed oneFootPerSecond = Speed.of(1.0, FEET_PER_SECOND); // will be reported in

        assertThat(oneKnot.toString()).isEqualTo("1.000kn");
        assertThat(oneMeterPerSecond.toString()).isEqualTo("1.000mps");
        assertThat(oneFootPerMinute.toString()).isEqualTo("1.000fpm");
        assertThat(oneFootPerSecond.toString()).isEqualTo("60.000fpm");
    }

    @Test
    public void toStringAndParsingEquivalence() {

        Speed startingSpeed = Speed.of(1.23456, KNOTS);

        assertThat(startingSpeed.toString(1)).isEqualTo("1.2kn");
        assertThat(startingSpeed.toString(5)).isEqualTo("1.23456kn");

        assertThat(Speed.fromString("1.23456kn")).isEqualTo(startingSpeed);
    }

    @Test
    public void testZeroConstant() {

        assertTrue(Speed.ZERO.isZero());
        assertEquals(
                Speed.ZERO.inFeetPerSecond(), Speed.of(0.0, FEET_PER_SECOND).inFeetPerSecond(), 1E-10);
    }

    @Test
    public void testAbsoluteValue() {

        Speed positiveSpeed = Speed.of(45.6, KNOTS);
        Speed zeroSpeed = Speed.of(0, METERS_PER_SECOND);
        Speed negativeSpeed = Speed.of(-12.3, FEET_PER_MINUTE);

        assertThat(Speed.of(45.6, KNOTS)).isEqualTo(positiveSpeed.abs());
        assertThat(Speed.of(0, METERS_PER_SECOND)).isEqualTo(zeroSpeed.abs());
        assertThat(Speed.of(12.3, FEET_PER_MINUTE)).isEqualTo(negativeSpeed.abs());
    }
}

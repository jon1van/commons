package io.github.jon1van.commons.units;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static io.github.jon1van.commons.units.Distance.*;
import static io.github.jon1van.commons.units.Distance.Unit.*;
import static io.github.jon1van.commons.units.Speed.Unit.MILES_PER_HOUR;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

public class DistanceTest {

    @Test
    public void testConstructor() {
        Distance dist = new Distance(1.0, NAUTICAL_MILES);

        double TOL = 0.00001;
        assertEquals(1.0, dist.inNauticalMiles(), TOL);
        assertEquals(1852.0, dist.inMeters(), 1852.0 * TOL);
        assertEquals(6076.12, dist.inFeet(), 6076.12 * TOL);
        assertEquals(1.15078, dist.inMiles(), 1.15078 * TOL);
    }

    @Test
    public void testOf() {

        Distance dist = Distance.of(1.0, METERS);

        double TOL = 0.00001;
        assertEquals(0.000539956803456, dist.inNauticalMiles(), 0.000539956803456 * TOL);
        assertEquals(1.0, dist.inMeters(), TOL);
        assertEquals(3.28084, dist.inFeet(), 3.28084 * TOL);
    }

    @Test
    public void testTheConverterMethod_in() {

        double TOL = 0.00001;

        Distance oneNm = Distance.ofNauticalMiles(1.0);
        assertEquals(1.0, oneNm.in(NAUTICAL_MILES), TOL);
        assertEquals(1852.0, oneNm.in(METERS), 1852.0 * TOL);
        assertEquals(6076.12, oneNm.in(FEET), 6076.12 * TOL);
        assertEquals(1.15078, oneNm.in(MILES), 1.15078 * TOL);

        Distance oneMeter = Distance.ofMeters(1.0);
        assertEquals(0.000539956803456, oneMeter.in(NAUTICAL_MILES), 0.000539956803456 * TOL);
        assertEquals(1.0, oneMeter.in(METERS), TOL);
        assertEquals(0.001, oneMeter.inKilometers(), TOL);
        assertEquals(3.28084, oneMeter.in(FEET), 3.28084 * TOL);
        assertEquals(0.000621371, oneMeter.in(MILES), 0.000621371 * TOL);
    }

    @Test
    public void testComparisonMethods() {

        Distance halfMeter = Distance.of(0.5, METERS);
        Distance oneMeter = Distance.of(1.0, METERS);
        Distance oneThousandMeters = Distance.of(1_000, METERS);
        Distance oneKiloMeter = Distance.of(1, KILOMETERS);

        assertTrue(halfMeter.isLessThan(oneMeter));
        assertTrue(halfMeter.isLessThanOrEqualTo(oneMeter));
        assertTrue(oneMeter.isGreaterThan(halfMeter));
        assertTrue(oneMeter.isGreaterThanOrEqualTo(halfMeter));

        assertTrue(oneKiloMeter.isGreaterThanOrEqualTo(oneThousandMeters));
        assertTrue(oneKiloMeter.isLessThanOrEqualTo(oneThousandMeters));
    }

    @Test
    public void testMiles() {
        Distance oneMile = Distance.ofMiles(1.0);
        assertEquals(1.0, oneMile.inMiles(), 0.00001);
        assertEquals(5_280.0, oneMile.inFeet(), 5_280.0 * 0.00001);
    }

    @Test
    public void testAbs() {
        Distance oneMeter = Distance.of(1, METERS);
        Distance negativeMeter = Distance.of(-1, METERS);

        assertThat(negativeMeter.abs()).isEqualTo(oneMeter);
        assertThat(oneMeter.abs()).isEqualTo(Distance.of(1.0, METERS));
    }

    @Test
    public void testNegate() {
        Distance oneMeter = Distance.of(1, METERS);
        Distance negativeMeter = Distance.of(-1, METERS);

        assertThat(oneMeter.negate()).isEqualTo(negativeMeter);
    }

    @Test
    public void testTimes() {
        Distance oneMeter = Distance.of(1, METERS);
        Distance halfMeter = oneMeter.times(0.5);

        double TOL = 0.00001;

        assertEquals(0.5, halfMeter.inMeters(), TOL);
    }

    @Test
    public void testPlus() {
        Distance oneFoot = Distance.ofFeet(1);
        Distance fiveHalvesFeet = Distance.of(2.5, FEET);

        Distance sum = oneFoot.plus(fiveHalvesFeet);

        double TOL = 0.00001;

        assertEquals(3.5, sum.inFeet(), TOL);
    }

    @Test
    public void testMinus() {
        Distance oneFoot = Distance.of(1, FEET);
        Distance fiveHalvesFeet = Distance.of(2.5, FEET);

        Distance sum = oneFoot.minus(fiveHalvesFeet);

        double TOL = 0.00001;

        assertEquals(-1.5, sum.inFeet(), TOL);
        assertTrue(sum.isNegative());
    }

    @Test
    public void testEquals() {

        Distance a = Distance.of(5, FEET);
        Distance b = Distance.of(5, FEET);
        Distance c = Distance.of(12, FEET); // same unit, different amount
        Distance d = Distance.of(5, NAUTICAL_MILES); // same amount, different unit
        Integer i = 12;

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a != b).isTrue();

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals(i)).isFalse();

        assertThat(a.equals(c)).isFalse();
        assertThat(a.equals(d)).isFalse();
    }

    @Test
    public void testCompareTo() {
        Distance oneMeter = Distance.of(1.0, METERS);
        Distance zero = Distance.of(0, NAUTICAL_MILES);
        Distance negative1Feet = Distance.of(-1, FEET);
        Distance oneFoot = Distance.of(1.0, FEET);
        Distance oneNm = Distance.of(1.0, NAUTICAL_MILES);
        Distance fourFeet = Distance.of(4.0, FEET);
        Distance oneKm = Distance.ofKiloMeters(1.0);
        Distance fiveFeet = Distance.of(5.0, FEET);

        List<Distance> distances =
                newArrayList(oneMeter, zero, oneKm, negative1Feet, oneFoot, oneNm, fourFeet, fiveFeet);

        Collections.sort(distances);
        assertEquals(distances.get(0), negative1Feet);
        assertEquals(distances.get(1), zero);
        assertEquals(distances.get(2), oneFoot);
        assertEquals(distances.get(3), oneMeter);
        assertEquals(distances.get(4), fourFeet);
        assertEquals(distances.get(5), fiveFeet);
        assertEquals(distances.get(6), oneKm);
        assertEquals(distances.get(7), oneNm);
    }

    @Test
    public void testToStringWithCustomLength() {
        // confirm the number of digits is reflected
        assertEquals("1m", Distance.ofMeters(1).toString(0));
        assertEquals("1.0m", Distance.ofMeters(1).toString(1));
        assertEquals("1.00m", Distance.ofMeters(1).toString(2));

        assertEquals("1.00m", Distance.ofMeters(1).toString(2));
        assertEquals("1.00km", Distance.ofKiloMeters(1).toString(2));
        assertEquals("1.00NM", Distance.ofNauticalMiles(1).toString(2));
        assertEquals("1.00ft", Distance.ofFeet(1).toString(2));
    }

    @Test
    public void testToString() {
        assertEquals("1.00m", Distance.ofMeters(1).toString());
        assertEquals("1.00000km", Distance.ofKiloMeters(1).toString());
        assertEquals("1.00ft", Distance.ofFeet(1).toString());
        assertEquals("1.00000NM", Distance.ofNauticalMiles(1).toString());
    }

    @Test
    public void testStaticFactory_DistanceBetween() {

        LatLong one = LatLong.of(1.0, 1.0);
        LatLong two = LatLong.of(2.0, 2.0);
        LatLong three = LatLong.of(1.0, 2.0);

        assertEquals(Distance.between(one, one), Distance.ofNauticalMiles(0));
        assertEquals(Distance.between(two, two), Distance.ofNauticalMiles(0));
        assertEquals(Distance.between(three, three), Distance.ofNauticalMiles(0));

        assertEquals(Distance.between(one, two), one.distanceTo(two));
        assertEquals(Distance.between(two, one), one.distanceTo(two));

        assertEquals(Distance.between(one, three), one.distanceTo(three));
        assertEquals(Distance.between(three, one), one.distanceTo(three));

        assertEquals(Distance.between(two, three), two.distanceTo(three));
        assertEquals(Distance.between(three, two), two.distanceTo(three));
    }

    @Test
    public void testDividedBy_distance() {

        Distance a = Distance.ofFeet(12);
        Distance b = Distance.ofFeet(24);

        double EPSILON = 0.0000001;
        assertThat(a.dividedBy(b)).isEqualTo(0.5, within(EPSILON));
        assertThat(b.dividedBy(a)).isEqualTo(2.0, within(EPSILON));
    }

    @Test
    public void testDividedBy_distance_differentUnits() {

        Distance a = Distance.ofFeet(12);
        Distance b = Distance.ofMeters(24);

        double EPSILON = 0.00001;
        assertThat(a.dividedBy(b)).isEqualTo(0.5 * (1.0 / 3.28084), within(EPSILON));
        assertThat(b.dividedBy(a)).isEqualTo(2.0 * 3.28084, within(EPSILON));
    }

    @Test
    public void testIsPositive() {
        Distance negativeOne = Distance.ofFeet(-1.0);
        assertFalse(negativeOne.isPositive());
        assertTrue(negativeOne.negate().isPositive());

        Distance zero = Distance.ofFeet(0.0);
        assertFalse(zero.isPositive());
        assertFalse(zero.negate().isPositive());

        Distance one = Distance.ofFeet(1.0);
        assertTrue(one.isPositive());
        assertFalse(one.negate().isPositive());
    }

    @Test
    public void testIsNegative() {
        Distance negativeOne = Distance.ofFeet(-1.0);
        assertTrue(negativeOne.isNegative());
        assertFalse(negativeOne.negate().isNegative());

        Distance zero = Distance.ofFeet(0.0);
        assertFalse(zero.isNegative());
        assertFalse(zero.negate().isNegative());

        Distance one = Distance.ofFeet(1.0);
        assertFalse(one.isNegative());
        assertTrue(one.negate().isNegative());
    }

    @Test
    public void testIsZero() {
        Distance negativeOne = Distance.ofFeet(-1.0);
        assertFalse(negativeOne.isZero());
        assertFalse(negativeOne.negate().isZero());

        Distance zero = Distance.ofFeet(0.0);
        assertTrue(zero.isZero());
        assertTrue(zero.negate().isZero());

        Distance one = Distance.ofFeet(1.0);
        assertFalse(one.isZero());
        assertFalse(one.negate().isZero());
    }

    @Test
    public void noNullInputToMean() {
        Distance[] distances = null;

        assertThrows(NullPointerException.class, () -> mean(distances));
    }

    @Test
    public void meanRequiresNonEmptyCollection() {
        assertThrows(IllegalArgumentException.class, () -> mean(newArrayList()));
    }

    @Test
    public void meanWorksWhenAllUnitsAreTheSame() {
        Distance average = mean(Distance.ofFeet(22), Distance.ofFeet(12));

        assertThat(average).isEqualTo(Distance.ofFeet(17));
    }

    @Test
    public void meanWorkWhenUnitsAreDifferent() {
        Distance average = mean(
                Distance.ofFeet(22), Distance.ofFeet(12), Distance.ofMeters(1) // 3.28084 ft
        );

        assertThat(average.nativeUnit()).isEqualTo(FEET);
        assertThat(average.inFeet()).isEqualTo(12.4269, within(0.0001));
    }

    @Test
    public void sumDoesNotAcceptNullInput() {
        Distance[] distances = null;

        assertThrows(NullPointerException.class, () -> sum(distances));
    }

    @Test
    public void sumAllowsEmptyCollection() {
        Distance result = sum(newArrayList());
        assertThat(result.inMeters()).isEqualTo(0.0);
    }

    @Test
    public void sumWorksWhenAllUnitsAreTheSame() {
        Distance sum = sum(Distance.ofFeet(22), Distance.ofFeet(12));

        assertThat(sum).isEqualTo(Distance.ofFeet(34));
    }

    @Test
    public void sumWorkWhenUnitsAreDifferent() {
        Distance average = sum(
                Distance.ofFeet(22), Distance.ofFeet(12), Distance.ofMeters(1) // 3.28084 ft
        );

        assertThat(average.nativeUnit()).isEqualTo(FEET);
        assertThat(average.inFeet()).isCloseTo(37.28084, within(0.0001));
    }

    @Test
    public void creatingSpeedByDividingByTime() {
        var speed = Distance.ofMiles(1.0).dividedBy(Duration.ofHours(2));
        assertThat(speed).isEqualTo(Speed.of(0.5, MILES_PER_HOUR));
    }

    @Test
    public void unitFromStringProperlyParsesUnits() {

        assertThat(unitFromString("ft")).isEqualTo(FEET);
        assertThat(unitFromString("5.0ft")).isEqualTo(FEET);

        assertThat(unitFromString("km")).isEqualTo(KILOMETERS);
        assertThat(unitFromString("5.0km")).isEqualTo(KILOMETERS);

        assertThat(unitFromString("m")).isEqualTo(METERS);
        assertThat(unitFromString("5.0m")).isEqualTo(METERS);

        assertThat(unitFromString("mi")).isEqualTo(Distance.Unit.MILES);
        assertThat(unitFromString("5.0mi")).isEqualTo(Distance.Unit.MILES);

        assertThat(unitFromString("NM")).isEqualTo(NAUTICAL_MILES);
        assertThat(unitFromString("5.0NM")).isEqualTo(NAUTICAL_MILES);

        assertThat(unitFromString("")).isNull();
        assertThat(unitFromString("notAUnit")).isNull();
        ;
    }

    @Test
    public void distanceFromString_noUnitFound() {
        assertThrows(IllegalArgumentException.class, () -> Distance.fromString("notAUnit"));
    }

    @Test
    public void distanceFromString_nullInput() {
        assertThrows(NullPointerException.class, () -> Distance.fromString(null));
    }

    @Test
    public void distanceFromString() {

        // can parse when there is no space
        assertThat(Distance.fromString("5.0ft")).isEqualTo(Distance.of(5, FEET));
        assertThat(Distance.fromString("5.0km")).isEqualTo(Distance.of(5, KILOMETERS));
        assertThat(Distance.fromString("5.0m")).isEqualTo(Distance.of(5, METERS));
        assertThat(Distance.fromString("5.0mi")).isEqualTo(Distance.of(5, MILES));
        assertThat(Distance.fromString("5.0NM")).isEqualTo(Distance.of(5, NAUTICAL_MILES));

        // can parse when there is a space
        assertThat(Distance.fromString("5.0 ft")).isEqualTo(Distance.of(5, FEET));
        assertThat(Distance.fromString("5.0 km")).isEqualTo(Distance.of(5, KILOMETERS));
        assertThat(Distance.fromString("5.0 m")).isEqualTo(Distance.of(5, METERS));
        assertThat(Distance.fromString("5.0 mi")).isEqualTo(Distance.of(5, MILES));
        assertThat(Distance.fromString("5.0 NM")).isEqualTo(Distance.of(5, NAUTICAL_MILES));

        // can parse different levels of accuracy
        assertThat(Distance.fromString("5 ft")).isEqualTo(Distance.of(5, FEET));
        assertThat(Distance.fromString("5.01 km")).isEqualTo(Distance.of(5.01, KILOMETERS));
        assertThat(Distance.fromString("5.12345 m")).isEqualTo(Distance.of(5.12345, METERS));
        assertThat(Distance.fromString("5E5 mi")).isEqualTo(Distance.of(5E5, MILES));
        assertThat(Distance.fromString("5.000001 NM")).isEqualTo(Distance.of(5.000001, NAUTICAL_MILES));

        // can parse zero and negative numbers
        assertThat(Distance.fromString("-5 ft")).isEqualTo(Distance.of(-5, FEET));
        assertThat(Distance.fromString("-5.01 km")).isEqualTo(Distance.of(-5.01, KILOMETERS));
        assertThat(Distance.fromString("-5.12345 m")).isEqualTo(Distance.of(-5.12345, METERS));
        assertThat(Distance.fromString("-5E5 mi")).isEqualTo(Distance.of(-5E5, MILES));
        assertThat(Distance.fromString("0 NM")).isEqualTo(Distance.of(0, NAUTICAL_MILES));
    }

    @Test
    public void min_returnsFirstArgumentWhenTied() {

        Distance oneHundred = Distance.ofFeet(100);
        Distance ondHundred_v2 = Distance.ofFeet(100);

        assertThat(oneHundred.equals(ondHundred_v2)).isTrue(); // they ARE equal
        assertThat(oneHundred == ondHundred_v2).isFalse();  // but they ARE DIFFERENT instances

        assertThat(min(oneHundred, ondHundred_v2)).isEqualTo(oneHundred);
    }

    @Test
    public void min_returnsTheMin() {

        Distance one = Distance.ofFeet(100);
        Distance two = Distance.ofFeet(200);

        assertThat(min(one, two)).isEqualTo(one);
        assertThat(min(two, one)).isEqualTo(one);
    }

    @Test
    public void min_minOfEmptyArrayIsNull() {
        assertThat(min(new Distance[]{})).isNull();
    }

    @Test
    public void min_minOfSingletonArrayIsFound() {
        assertThat(min(new Distance[]{Distance.ofFeet(100)})).isEqualTo(Distance.ofFeet(100));
    }

    @Test
    public void min_minOfLongArrayIsFound() {
        Distance[] testData_1 =
                new Distance[]{Distance.ofFeet(1000.0), Distance.ofMeters(1), Distance.ofNauticalMiles(1.0)};
        Distance[] testData_2 = new Distance[]{
                Distance.ofMeters(1), Distance.ofNauticalMiles(1.0), Distance.ofFeet(1000.0),
        };

        assertThat(min(testData_1)).isEqualTo(Distance.ofMeters(1));
        assertThat(min(testData_2)).isEqualTo(Distance.ofMeters(1));
    }

    @Test
    public void max_returnsFirstArgumentWhenTied() {

        Distance oneHundred = Distance.ofFeet(100);
        Distance ondHundred_v2 = Distance.ofFeet(100);

        assertThat(oneHundred.equals(ondHundred_v2)).isTrue(); // they ARE equal
        assertThat(oneHundred == ondHundred_v2).isFalse();  // but they ARE DIFFERENT instances

        assertThat(max(oneHundred, ondHundred_v2)).isEqualTo(oneHundred);
    }

    @Test
    public void max_returnsTheMax() {

        Distance one = Distance.ofFeet(100);
        Distance two = Distance.ofFeet(200);

        assertThat(max(one, two)).isEqualTo(two);
        assertThat(max(two, one)).isEqualTo(two);
    }

    @Test
    public void max_maxOfEmptyArrayIsNull() {
        var emptyArray = new Distance[]{};
        assertThat(max(emptyArray)).isNull();
    }

    @Test
    public void max_maxOfSingletonArrayIsFound() {
        assertThat(max(new Distance[]{Distance.ofFeet(100)})).isEqualTo(Distance.ofFeet(100));
    }

    @Test
    public void max_maxOfLongArrayIsFound() {
        Distance[] testData_1 = new Distance[]{
                Distance.ofFeet(1000.0), Distance.ofMeters(1), Distance.ofNauticalMiles(1.0)
        };
        Distance[] testData_2 = new Distance[]{
                Distance.ofMeters(1), Distance.ofNauticalMiles(1.0), Distance.ofFeet(1000.0)
        };

        assertThat(max(testData_1)).isEqualTo(Distance.ofNauticalMiles(1));
        assertThat(max(testData_2)).isEqualTo(Distance.ofNauticalMiles(1));
    }

    @Test
    public void testZeroConstant() {

        assertThat(Distance.ZERO.isZero()).isTrue();
        assertThat(Distance.ZERO.inFeet()).isEqualTo(Distance.of(0.0, FEET).inFeet(), within(1E-10));
    }
}

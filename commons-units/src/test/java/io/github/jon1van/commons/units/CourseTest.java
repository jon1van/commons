package io.github.jon1van.commons.units;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static io.github.jon1van.commons.units.Course.Unit.DEGREES;
import static io.github.jon1van.commons.units.Course.Unit.RADIANS;
import static io.github.jon1van.commons.units.Course.angleBetween;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.*;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;



public class CourseTest {

    private static final double TOLERANCE = 0.0000001;

    @Test
    public void testConstructor() {
        Course oneDegree = new Course(1.0, DEGREES);
        assertThat(oneDegree.inDegrees()).isCloseTo(1.0, within(TOLERANCE));
        assertThat(oneDegree.nativeUnit()).isEqualTo(DEGREES);

        Course twoRadian = new Course(2.0, RADIANS);
        assertThat(twoRadian.inRadians()).isCloseTo(2.0, within(TOLERANCE));
        assertThat(twoRadian.nativeUnit()).isEqualTo(RADIANS);
    }

    @Test
    public void testConstructionViaOfDegrees() {
        Course oneDegree = Course.ofDegrees(1.0);
        assertThat(oneDegree.inDegrees()).isCloseTo(1.0, within(TOLERANCE));
        assertThat(oneDegree.nativeUnit()).isEqualTo(DEGREES);
    }

    @Test
    public void testConstructionViaOfRadians() {
        Course twoRadian = Course.ofRadians(2.0);
        assertThat(twoRadian.inRadians()).isCloseTo(2.0, within(TOLERANCE));
        assertThat(twoRadian.nativeUnit()).isEqualTo(RADIANS);
    }

    @Test
    public void testUnitConversion() {

        Course ninetyDegrees = Course.ofDegrees(90.0);
        assertThat(ninetyDegrees.inRadians()).isCloseTo(PI / 2.0, within(TOLERANCE));

        Course threeSixtyDegrees = Course.ofDegrees(360.0);
        assertThat(threeSixtyDegrees.inRadians()).isCloseTo(2.0 * PI, within(TOLERANCE));

        Course zeroDegrees = Course.ofDegrees(0);
        assertThat(zeroDegrees.inRadians()).isCloseTo(0.0, within(TOLERANCE));

        Course twoRadian = Course.ofRadians(2.0);
        assertThat(twoRadian.inDegrees()).isCloseTo(2.0 * 180.0 / PI, within(TOLERANCE));

        Course piRadian = Course.ofRadians(PI);
        assertThat(piRadian.inDegrees()).isCloseTo(180.0, within(TOLERANCE));

        Course zeroRadians = Course.ofRadians(0.0);
        assertThat(zeroRadians.inDegrees()).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    public void testUnitConversion_negatives() {

        Course ninetyDegrees = Course.ofDegrees(-90.0);
        assertThat(ninetyDegrees.inRadians()).isCloseTo(-PI / 2.0, within(TOLERANCE));

        Course threeSixtyDegrees = Course.ofDegrees(-360.0);
        assertThat(threeSixtyDegrees.inRadians()).isCloseTo(-2.0 * PI, within(TOLERANCE));

        Course zeroDegrees = Course.ofDegrees(-0);
        assertThat(zeroDegrees.inRadians()).isCloseTo(-0.0, within(TOLERANCE));

        Course twoRadian = Course.ofRadians(-2.0);
        assertThat(twoRadian.inDegrees()).isCloseTo(-2.0 * 180.0 / PI, within(TOLERANCE));

        Course piRadian = Course.ofRadians(-PI);
        assertThat(piRadian.inDegrees()).isCloseTo(-180.0, within(TOLERANCE));

        Course zeroRadians = Course.ofRadians(-0.0);
        assertThat(zeroRadians.inDegrees()).isCloseTo(-0.0, within(TOLERANCE));
    }

    @Test
    public void testNegate() {
        Course ninetyDegrees = Course.ofDegrees(90.0);
        Course negativeNinetyDegrees = Course.ofDegrees(-90.0);

        assertThat(ninetyDegrees.negate()).isEqualTo(negativeNinetyDegrees);
        assertThat(negativeNinetyDegrees.negate()).isEqualTo(ninetyDegrees);
    }

    @Test
    public void testAbs() {
        Course ninetyDegrees = Course.ofDegrees(90.0);
        Course negativeNinetyDegrees = Course.ofDegrees(-90.0);

        assertThat(ninetyDegrees.abs()).isEqualTo(ninetyDegrees);
        assertThat(negativeNinetyDegrees.abs()).isEqualTo(ninetyDegrees);
    }

    @Test
    public void testToString_int() {
        assertEquals("90deg", Course.ofDegrees(90.0).toString(0));
        assertEquals("3rad", Course.ofRadians(PI).toString(0));
    }

    @Test
    public void testToString() {
        assertEquals("90deg", Course.ofDegrees(90.0).toString());
        assertEquals("3.14159rad", Course.ofRadians(PI).toString());
    }

    @Test
    public void testIsPositive() {
        assertThat(Course.ofDegrees(20).isPositive()).isTrue();
        assertThat(Course.ofDegrees(-20).isPositive()).isFalse();
        assertThat(Course.ofDegrees(0).isPositive()).isFalse();

        assertThat(Course.ofRadians(1).isPositive()).isTrue();
        assertThat(Course.ofRadians(-1).isPositive()).isFalse();
        assertThat(Course.ofRadians(0).isPositive()).isFalse();
    }

    @Test
    public void testIsNegative() {
        assertThat(Course.ofDegrees(20).isNegative()).isFalse();
        assertThat(Course.ofDegrees(-20).isNegative()).isTrue();
        assertThat(Course.ofDegrees(0).isNegative()).isFalse();

        assertThat(Course.ofRadians(1).isNegative()).isFalse();
        assertThat(Course.ofRadians(-1).isNegative()).isTrue();
        assertThat(Course.ofRadians(0).isNegative()).isFalse();
    }

    @Test
    public void testTimes() {
        assertThat(Course.ofDegrees(20).times(5)).isEqualTo(Course.ofDegrees(100));
        assertThat(Course.ofDegrees(20).times(-5)).isEqualTo(Course.ofDegrees(-100));
        assertThat(Course.ofDegrees(20).times(0.5)).isEqualTo(Course.ofDegrees(10));
        assertThat(Course.ofDegrees(20).times(0)).isEqualTo(Course.ofDegrees(0));
    }

    @Test
    public void testDividedBy() {
        Course piRadians = Course.ofRadians(PI);
        Course twoPiRadians = Course.ofRadians(2 * PI);
        Course negativeNinetyDegrees = Course.ofDegrees(-90.0);

        assertThat(piRadians.dividedBy(piRadians)).isEqualTo(1.0);
        assertThat(piRadians.dividedBy(twoPiRadians)).isEqualTo(0.5);
        assertThat(twoPiRadians.dividedBy(piRadians)).isEqualTo(2.0);
        assertThat(negativeNinetyDegrees.dividedBy(piRadians)).isEqualTo(-0.50);
        assertThat(piRadians.dividedBy(negativeNinetyDegrees)).isEqualTo(-2.0);
    }

    @Test
    public void testComparisionMethods() {
        Course zeroRadians = Course.ofRadians(0);
        Course zeroDegrees = Course.ofDegrees(0);

        assertThat(zeroRadians.isGreaterThanOrEqualTo(zeroDegrees)).isTrue();
        assertThat(zeroDegrees.isGreaterThanOrEqualTo(zeroRadians)).isTrue();
        assertThat(zeroRadians.isGreaterThan(zeroDegrees)).isFalse();
        assertThat(zeroDegrees.isGreaterThan(zeroRadians)).isFalse();

        assertThat(zeroRadians.isLessThanOrEqualTo(zeroDegrees)).isTrue();
        assertThat(zeroDegrees.isLessThanOrEqualTo(zeroRadians)).isTrue();
        assertThat(zeroRadians.isLessThan(zeroDegrees)).isFalse();
        assertThat(zeroDegrees.isLessThan(zeroRadians)).isFalse();

        Course oneDegree = Course.ofDegrees(1);

        assertThat(oneDegree.isGreaterThanOrEqualTo(zeroDegrees)).isTrue();
        assertThat(oneDegree.isGreaterThan(zeroDegrees)).isTrue();
        assertThat(oneDegree.isGreaterThanOrEqualTo(zeroDegrees)).isTrue();
        assertThat(oneDegree.isLessThanOrEqualTo(zeroDegrees)).isFalse();
        assertThat(oneDegree.isLessThan(zeroDegrees)).isFalse();
    }

    @Test
    public void testPlus() {
        Course oneRadian = Course.ofRadians(1);
        Course ninetyDegrees = Course.ofDegrees(90);

        // native unit is taken from first argument
        assertThat(oneRadian.plus(ninetyDegrees).nativeUnit()).isEqualTo(RADIANS);
        assertThat(ninetyDegrees.plus(oneRadian).nativeUnit()).isEqualTo(DEGREES);

        // result is the same regardless of order
        assertThat(oneRadian.plus(ninetyDegrees).inRadians()).isEqualTo(1.0 + PI / 2.0);
        assertThat(ninetyDegrees.plus(oneRadian).inRadians()).isEqualTo(1.0 + PI / 2.0);
    }

    @Test
    public void testMinus() {
        Course oneRadian = Course.ofRadians(1);
        Course ninetyDegrees = Course.ofDegrees(90);

        // native unit is taken from first argument
        assertThat(oneRadian.minus(ninetyDegrees).nativeUnit()).isEqualTo(RADIANS);
        assertThat(ninetyDegrees.minus(oneRadian).nativeUnit()).isEqualTo(DEGREES);

        // order is reflected
        assertThat(oneRadian.minus(ninetyDegrees).inRadians()).isEqualTo(1.0 - PI / 2.0);
        assertThat(ninetyDegrees.minus(oneRadian).inRadians()).isEqualTo(PI / 2.0 - 1.0);
    }

    @Test
    public void testCompareTo() {
        // test via List sorting...

        Course negativeOneRadian = Course.ofRadians(-1.0);
        Course tenDegrees = Course.ofDegrees(10.0);
        Course twentyDegrees = Course.ofDegrees(20.0);
        Course twohundredDegrees = Course.ofDegrees(200.0);
        Course oneRadian = Course.ofRadians(1.0);
        Course twoPiRadians = Course.ofRadians(2 * PI);

        Course[] courses =
                new Course[] {oneRadian, twentyDegrees, tenDegrees, twoPiRadians, negativeOneRadian, twohundredDegrees};

        Arrays.sort(courses);

        assertThat(courses[0]).isEqualTo(negativeOneRadian);
        assertThat(courses[1]).isEqualTo(tenDegrees);
        assertThat(courses[2]).isEqualTo(twentyDegrees);
        assertThat(courses[3]).isEqualTo(oneRadian);
        assertThat(courses[4]).isEqualTo(twohundredDegrees);
        assertThat(courses[5]).isEqualTo(twoPiRadians);
    }

    @Test
    public void testAngleDifference() {
        assertThat(angleBetween(Course.ofDegrees(5.0), Course.ofDegrees(355.0))).isEqualTo(Course.ofDegrees(10));

        assertThat(angleBetween(Course.ofDegrees(355.0), Course.ofDegrees(5.0))).isEqualTo(Course.ofDegrees(-10));
    }

    @Test
    public void testHashcode() {
        Course negativeOneRadian = Course.ofRadians(-1.0);
        Course tenDegrees = Course.ofDegrees(10.0);
        Course twentyDegrees = Course.ofDegrees(20.0);
        Course twohundredDegrees = Course.ofDegrees(200.0);
        Course oneRadian = Course.ofRadians(1.0);
        Course twoPiRadians = Course.ofRadians(2 * PI);

        Set<Integer> hashes = new HashSet<>();
        hashes.add(negativeOneRadian.hashCode());
        hashes.add(tenDegrees.hashCode());
        hashes.add(twentyDegrees.hashCode());
        hashes.add(twohundredDegrees.hashCode());
        hashes.add(oneRadian.hashCode());
        hashes.add(twoPiRadians.hashCode());

        assertThat(hashes.size()).isEqualTo(6);
    }

    @Test
    public void testEquals() {
        Course zeroDegrees = Course.ofDegrees(0);
        Course oneDegree = Course.ofDegrees(1);

        assertThat(zeroDegrees.equals(oneDegree)).isFalse();
        assertThat(oneDegree.equals(zeroDegrees)).isFalse();
        assertThat(oneDegree.equals(null)).isFalse();
        assertThat(oneDegree.equals("not a course")).isFalse();
    }

    @Test
    public void equalsReflectsTheUnit() {

        Course zeroDegrees = Course.ofDegrees(0);
        Course zeroRadians = Course.ofRadians(0);

        // both ARE ZERO
        assertThat(zeroDegrees.isZero()).isTrue();
        assertThat(zeroRadians.isZero()).isTrue();

        // they are not equal
        assertThat(zeroRadians.equals(zeroDegrees)).isFalse();
    }

    @Test
    public void testSin() {
        assertThat(Course.ofDegrees(-90).sin()).isEqualTo(-1.0);
        assertThat(Course.ofDegrees(-45).sin()).isCloseTo(-sqrt(2.0) / 2.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(0).sin()).isEqualTo(0.0);
        assertThat(Course.ofDegrees(45).sin()).isCloseTo(sqrt(2.0) / 2.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(90).sin()).isEqualTo(1.0);
    }

    @Test
    public void testCos() {
        assertThat(Course.ofDegrees(-90).cos()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(-45).cos()).isCloseTo(sqrt(2.0) / 2.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(0).cos()).isEqualTo(1.0);
        assertThat(Course.ofDegrees(45).cos()).isCloseTo(sqrt(2.0) / 2.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(90).cos()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(135).cos()).isCloseTo(-sqrt(2.0) / 2.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(180).cos()).isEqualTo(-1.0);
    }

    @Test
    public void testTan() {
        assertThat(Course.ofDegrees(-45).tan()).isCloseTo(-1.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(0).tan()).isEqualTo(0.0);
        assertThat(Course.ofDegrees(45).tan()).isCloseTo(1.0, within(TOLERANCE));
        assertThat(Course.ofDegrees(90).tan()).isGreaterThan(2E14); // HUGE number...but won't be INFINITY
        assertThat(Course.ofDegrees(135).tan()).isCloseTo(-1, within(TOLERANCE));
    }

    @Test
    public void testZeroConstant() {

        assertTrue(Course.ZERO.isZero());
        assertThat(Course.ZERO.inDegrees()).isCloseTo(Course.of(0.0, DEGREES).inDegrees(), within(1E-10));
    }

    //	/**
    //	 * Test of between method, of class Course.
    //	 */
    //	@Test
    //	public void testBetween() {
    //		System.out.println("between");
    //		Course one = null;
    //		Course two = null;
    //		Course expResult = null;
    //		Course result = Course.between(one, two);
    //		assertEquals(expResult, result);
    //		// TODO review the generated test code and remove the default call to fail.
    //		fail("The test case is a prototype.");
    //	}

    //
    //	/**
    //	 * Test of isZero method, of class Course.
    //	 */
    //	@Test
    //	public void testIsZero() {
    //		System.out.println("isZero");
    //		Course instance = null;
    //		boolean expResult = false;
    //		boolean result = instance.isZero();
    //		assertEquals(expResult, result);
    //		// TODO review the generated test code and remove the default call to fail.
    //		fail("The test case is a prototype.");
    //	}
    //
    //
    //	/**
    //	 * Test of angleDifference method, of class Course.
    //	 */
    //	@Test
    //	public void testAngleDifference_Double_Double() {
    //		System.out.println("angleDifference");
    //		Double hdg = null;
    //		Double hdg0 = null;
    //		Double expResult = null;
    //		Double result = Course.angleDifference(hdg, hdg0);
    //		assertEquals(expResult, result);
    //		// TODO review the generated test code and remove the default call to fail.
    //		fail("The test case is a prototype.");
    //	}
    //
    //	/**
    //	 * Test of angleDifference method, of class Course.
    //	 */
    //	@Test
    //	public void testAngleDifference_Double() {
    //		System.out.println("angleDifference");
    //		Double dz = null;
    //		Double expResult = null;
    //		Double result = Course.angleDifference(dz);
    //		assertEquals(expResult, result);
    //		// TODO review the generated test code and remove the default call to fail.
    //		fail("The test case is a prototype.");
    //	}

}

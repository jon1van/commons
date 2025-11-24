package io.github.jon1van.units;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.hypot;
import static org.apache.commons.math3.util.FastMath.sin;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/// Anything that has a Latitude Longitude location
///
/// This interface provides methods to measure the distance and direction between objects
@SuppressWarnings("ALL")
public interface HasLatLong {
    double latitude();

    double longitude();

    /// [#latLong()] should usually be preferred over [#latLong128()] because a LatLong is accurate to 7 decimal places
    /// (i.e. about 11 millimeters resolution). Location data is rarely measure with enough precision to warrant the
    /// additional accuracy (and space consumption) that [LatLong128] provides.  Note: this nuance is only relevant
    /// if
    /// your system is storing millions of location measurements.
    ///
    /// @return A LatLong object which compactly contains the location of this object.
    default LatLong latLong() {
        return LatLong.of(latitude(), longitude());
    }

    /// [#latLong128()] should only be used when the underlying location data is measured with extremely high accuracy
    /// (e.g. <11 millimeters in error)
    ///
    /// @return A LatLong128 object which contains the location of this object.
    default LatLong128 latLong128() {
        return LatLong128.of(latitude(), longitude());
    }

    default Distance distanceTo(HasLatLong that) {
        return Navigation.distanceBtw(this, that);
    }

    /// @param other An object with a known LatLong
    /// @return The distance in Nautical Miles the provided object
    default double distanceInNmTo(HasLatLong other) {
        return distanceTo(other).inNauticalMiles();
    }

    /// @return The course (i.e. direction of travel) from this object to the other object.
    default double courseInDegrees(HasLatLong that) {
        return Navigation.courseInDegrees(
                latitude(), longitude(),
                that.latitude(), that.longitude());
    }

    /// @return The Course (i.e. direction of travel) from this object to the other object.
    default Course courseTo(HasLatLong that) {
        return Course.ofDegrees(courseInDegrees(that));
    }

    /// @param distance The maximum qualifying distance (inclusive)
    /// @param location The "other"
    /// @return True if this object's LatLong is within the specified Distance to the provided location.
    default boolean isWithin(Distance distance, LatLong128 location) {
        return this.distanceTo(location).isLessThanOrEqualTo(distance);
    }

    /// @return The location you'd arrive if you travel from this location the direction and distance given
    default HasLatLong move(Course course, Double distance) {
        return move(course.inDegrees(), distance);
    }

    /// Find the location you would arrive at if you travel from this location the direction and distance given
    ///
    /// @param course   The direction of travel (in degrees)
    /// @param distance The distance traveled (in nautical miles)
    /// @return The destination
    default HasLatLong move(Double course, Double distance) {
        return Navigation.move(latitude(), longitude(), course, distance);
    }

    default double distanceInRadians(HasLatLong that) {
        return Navigation.distanceInRadians(this.distanceInNmTo(that));
    }

    /// Find a new LatLong by projecting out from this location in a specific direction and distance.
    ///
    /// @param direction The direction of travel
    /// @param distance  The distance traveled
    /// @return The destination
    default LatLong move(Course direction, Distance distance) {
        return Navigation.move(this, direction, distance);
    }

    /// Find a new LatLong by projecting out from this location in a specific direction, distance, and curvature.
    ///
    /// @param direction The direction of travel
    /// @param distance  The distance traveled
    /// @param curvature The curvature of travel
    /// @return The destination
    default LatLong128 move(Double direction, Double distance, Double curvature) {
        return Navigation.move(latitude(), longitude(), direction, distance, curvature);
    }

    default LatLong128 greatCircleOrigin(Double course) {
        return Navigation.greatCircleOrigin(latitude(), longitude(), course);
    }

    static Double maxLatitude(Collection<? extends HasLatLong> locations) {
        checkInput(locations);

        return locations.stream().map(HasLatLong::latitude).reduce(-Double.MAX_VALUE, Math::max);
    }

    static Double minLatitude(Collection<? extends HasLatLong> locations) {
        checkInput(locations);

        return locations.stream().map(HasLatLong::latitude).reduce(Double.MAX_VALUE, Math::min);
    }

    static Double maxLongitude(Collection<? extends HasLatLong> locations) {
        checkInput(locations);

        return locations.stream().map(HasLatLong::longitude).reduce(-Double.MAX_VALUE, Math::max);
    }

    static Double minLongitude(Collection<? extends HasLatLong> locations) {
        checkInput(locations);

        return locations.stream().map(HasLatLong::longitude).reduce(Double.MAX_VALUE, Math::min);
    }

    static void checkInput(Collection<? extends HasLatLong> locations) {
        checkNotNull(locations, "The Collection of HasPositions cannot be null");
        checkArgument(!locations.isEmpty(), "The Collection of HasPositions cannot be empty");
    }

    static HasLatLong from(Double lat, Double lon) {
        return new LatLong128(lat, lon);
    }

    /// ACCURATELY compute the average LatLong positions of these two locations. The underlying computation performs
    /// several somewhat expensive trig operations. Consequently, you should consider using the quick version if the
    /// distance between the two input points is small.
    ///
    /// @param one The first location
    /// @param two The second location
    /// @return The average location
    static HasLatLong avgLatLong(HasLatLong one, HasLatLong two) {
        checkNotNull(one);
        checkNotNull(two);

        SphericalUnitVector vectorOne = new SphericalUnitVector(one);
        SphericalUnitVector vectorTwo = new SphericalUnitVector(two);

        double avgX = (vectorOne.x + vectorTwo.x) / 2.0;
        double avgY = (vectorOne.y + vectorTwo.y) / 2.0;
        double avgZ = (vectorOne.z + vectorTwo.z) / 2.0;

        double avgLong = atan2(avgY, avgX);
        double avgSqareRoot = hypot(avgX, avgY);
        double avgLat = atan2(avgZ, avgSqareRoot);

        return LatLong128.of(toDegrees(avgLat), toDegrees(avgLong));
    }

    /// QUICKLY compute the arithmetic average of LatLong positions of these two locations. This computation does not
    /// reflect curvature of the earth but it does correct for the international date line. The difference between
    // the
    /// result computed by this method and the result computed by avgLatLong grows as (1) the distance between the
    // two

    /// input points grows and (2) the points move further and further away from the equator.
    ///
    /// @param one The first location
    /// @param two The second location
    /// @return The average location
    static LatLong128 quickAvgLatLong(HasLatLong one, HasLatLong two) {
        // latitude never wraps, so arithmatic average is fine
        double averageLat = (one.latitude() + two.latitude()) / 2.0;

        // be careful with longitude -- the international date line is a problem
        double averageLong = (abs(one.longitude() - two.longitude()) > 180.0)
                ? ((one.longitude() + 180.0) + (two.longitude() + 180.0)) / 2.0
                : (one.longitude() + two.longitude()) / 2.0;

        return LatLong128.of(averageLat, averageLong);
    }

    /// ACCURATELY compute the average LatLong positions of these locations. The underlying computation performs several
    /// somewhat expensive trig operations when converting the LatLong data to Spherical Unit Vectors.
    ///
    /// @param locations An array of LatLong locations
    /// @return The average location
    /// @throws NoSuchElementException When locations is empty
    static LatLong avgLatLong(HasLatLong... locations) {
        //  static LatLong128 avgLatLong(LatLong128... locations) {
        requireNonNull(locations);

        if (locations.length == 0) {
            throw new NoSuchElementException("Average LatLong not defined when empty");
        }

        double x = 0;
        double y = 0;
        double z = 0;
        for (HasLatLong location : locations) {
            SphericalUnitVector vector = new SphericalUnitVector(location);
            x += vector.x;
            y += vector.y;
            z += vector.z;
        }
        x /= locations.length;
        y /= locations.length;
        z /= locations.length;

        double avgLong = atan2(y, x);
        double avgSqareRoot = hypot(x, y);
        double avgLat = atan2(z, avgSqareRoot);

        return LatLong.of(toDegrees(avgLat), toDegrees(avgLong));
    }

    /// ACCURATELY compute the average LatLong positions of these locations. The underlying computation performs several
    /// somewhat expensive trig operations when converting the LatLong data to Spherical Unit Vectors.
    ///
    /// @param locations A collection of LatLong locations
    /// @return The average location
    /// @throws NoSuchElementException When locations is empty
    static LatLong avgLatLong(Collection<? extends HasLatLong> locations) {
        requireNonNull(locations);

        LatLong128[] asArray = locations.stream().map(p -> p.latLong128()).toArray(LatLong128[]::new);

        return avgLatLong(asArray);
    }

    /// QUICKLY compute the ARITHMETIC average of these LatLong positions. This computation does not reflect curvature
    /// of the earth, but it does correct for the international date line. The difference between the result
    // computed
    /// by this method and the result computed by `avgLatLong()` grows as (1) the path distance grows and (2) the
    // path
    /// locations move further and further away from the equator.
    ///
    /// This method is FASTER and LESS ACCURATE because it utilizes simple arithmetic instead of accurate
    /// trigonometric functions.
    ///
    /// @param locations An array of locations
    /// @return The average location
    /// @throws NoSuchElementException When locations is empty
    static LatLong quickAvgLatLong(HasLatLong... locations) {
        requireNonNull(locations);

        if (locations.length == 0) {
            throw new NoSuchElementException("The input array was empty");
        }

        if (locations.length == 1) {
            return locations[0].latLong();
        }

        // just take the simple average of latitude values....
        double avgLatitude = Stream.of(locations)
                .mapToDouble(loc -> loc.latitude())
                .average()
                .getAsDouble();
        // longitude cannot be simply averaged due to discontinuity when -180 abuts 180
        // So, we are going to take several "weighted averages of TWO Longitude values"
        // We can correct for the international date line with every subsequent avg.
        double[] longitudes =
                Stream.of(locations).mapToDouble(loc -> loc.longitude()).toArray();

        // average the first two entries, then average in the 3rd entry, then the 4th...
        // increase the "weight" on the "curAverage" each time through the loop
        double curAvgLongitude = longitudes[0];
        for (int i = 1; i < longitudes.length; i++) {
            curAvgLongitude = avgLong(curAvgLongitude, i, longitudes[i], 1);
        }

        return LatLong.of(avgLatitude, curAvgLongitude);
    }

    /// QUICKLY compute the ARITHMETIC average of these LatLong positions. This computation does not reflect curvature
    /// of the earth, but it does correct for the international date line. The difference between the result
    /// computed by this method and the result computed by `avgLatLong()` grows as (1) the path distance grows and
    // (2) the path
    /// locations move further and further away from the equator. This method is FASTER and LESS ACCURATE because it
    /// utilizes simple arithmetic instead of accurate trigonometric  functions.
    ///
    /// @param locations A collection of LatLong locations
    /// @return The average location
    /// @throws NoSuchElementException When locations is empty
    static LatLong quickAvgLatLong(Collection<? extends HasLatLong> locations) {
        requireNonNull(locations);
        return quickAvgLatLong(locations.toArray(new HasLatLong[0]));
    }

    /// Naively compute the weighted average of two longitude values. Be careful, This method ignores curvature of the
    /// earth.
    private static double avgLong(double longitudeA, int weightA, double longitudeB, int weightB) {

        double w1 = (double) (weightA) / (double) (weightA + weightB);
        double w2 = (double) (weightB) / (double) (weightA + weightB);

        double averageLong = (abs(longitudeA - longitudeB) > 180.0)
                ? w1 * (longitudeA + 180.0) + w2 * (longitudeB + 180.0)
                : w1 * longitudeA + w2 * longitudeB;

        return averageLong;
    }

    /// Models a LatLong location as a 3-dimensional unit vector. This conversion can be useful for eliminating some
    /// corner cases involved with certain LatLong operations (like find the average LatLong across the
    // international
    /// date line).
    class SphericalUnitVector {

        final double x;
        final double y;
        final double z;

        private SphericalUnitVector(HasLatLong location) {
            double latInRadian = toRadians(location.latitude());
            double longInRadian = toRadians(location.longitude());
            this.x = cos(latInRadian) * cos(longInRadian);
            this.y = cos(latInRadian) * sin(longInRadian);
            this.z = sin(latInRadian);
        }
    }
}

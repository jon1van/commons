package io.github.jon1van.commons.units;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/// Anything that has a Latitude Longitude location
///
/// This interface provides methods to measure the distance and direction between objects
public interface HasLatLong {

    double latitude();

    double longitude();

    /// [#latLong()] should usually be preferred over [#latLong128()] because a LatLong is accurate to 7 decimal places
    /// (i.e. about 11 millimeters resolution). Location data is rarely measure with enough precision to warrant the
    /// additional accuracy (and space consumption) that [LatLong128] provides.  Note: this nuance is only relevant if
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
        return Spherical.distanceBtw(this, that);
    }

    /// @param other An object with a known LatLong
    ///
    /// @return The distance in Nautical Miles the provided object
    default double distanceInNmTo(HasLatLong other) {
        return distanceTo(other).inNauticalMiles();
    }

    /// @return The course (i.e. direction of travel) from this object to the other object.
    default double courseInDegrees(HasLatLong that) {
        return Spherical.courseInDegrees(
                latitude(), longitude(),
                that.latitude(), that.longitude()
        );
    }

    /// @return The Course (i.e. direction of travel) from this object to the other object.
    default Course courseTo(HasLatLong that) {
        return Course.ofDegrees(courseInDegrees(that));
    }



    /// @param distance The maximum qualifying distance (inclusive)
    /// @param location The "other"
    ///
    /// @return True if this object's LatLong is within the specified Distance to the provided location.
    default boolean isWithin(Distance distance, LatLong128 location) {
        return this.distanceTo(location).isLessThanOrEqualTo(distance);
    }




    /// @return The location you'd arrive if you travel from this location the direction and distance given
    default LatLong128 projectOut(Double course, Double distance) {
        return Spherical.projectOut(latitude(), longitude(), course, distance);
    }

    default double distanceInRadians(HasLatLong that) {
        return Spherical.distanceInRadians(this.distanceInNmTo(that));
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
}

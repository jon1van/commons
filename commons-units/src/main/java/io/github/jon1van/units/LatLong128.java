package io.github.jon1van.units;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.Base64;

import com.google.common.collect.ComparisonChain;
import org.jspecify.annotations.NonNull;

/// LatLong128 is an Immutable pair of doubles that is always checked to ensure validity.
///
/// LatLong128 is twice the size of [LatLong] because it uses an obvious, but less compact method to store the latitude
/// and longitude values. If you plan to store LOTS of location data consider the tradeoff in space and accuracy between
/// these two classes.
public record LatLong128(double latitude, double longitude) implements HasLatLong, Comparable<LatLong128> {

    /// LatLong128 records will always have valid latitude (-90 to 90) and longitude (-180 to 180) values
    public LatLong128 {
        checkLatitude(latitude);
        checkLongitude(longitude);
    }

    /// Create a new LatLong object.
    ///
    /// @param latitude  A non-null Latitude value from (-90 to 90)
    /// @param longitude A non-null Longitude value from (-180 to 180)
    /// @return A newly created LatLong object
    public static LatLong128 of(Double latitude, Double longitude) {
        return new LatLong128(latitude, longitude);
    }

    /// Create a new LatLong object.
    ///
    /// @param exactly16Bytes The bytes defining two doubles: {latitude, longitude}
    /// @return A new LatLong object.
    public static LatLong128 fromBytes(byte[] exactly16Bytes) {
        requireNonNull(exactly16Bytes);
        checkArgument(exactly16Bytes.length == 16, "Must use exactly 16 bytes");
        long bigBits = 0; // e.g. most significant bits
        long smallBits = 0; // e.g. least significant bits
        for (int i = 0; i < 8; i++) {
            bigBits = (bigBits << 8) | (exactly16Bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            smallBits = (smallBits << 8) | (exactly16Bytes[i] & 0xff);
        }
        double longitude = Double.longBitsToDouble(bigBits);
        double latitude = Double.longBitsToDouble(smallBits);

        return LatLong128.of(longitude, latitude);
    }

    /// Create a new LatLong object.
    ///
    /// @param base64Encoding The Base64 safe and URL safe (no padding) encoding of a LatLong's byte[]
    /// @return A new LatLong object.
    public static LatLong128 fromBase64Str(String base64Encoding) {
        return LatLong128.fromBytes(Base64.getUrlDecoder().decode(base64Encoding));
    }

    /// Throw an IllegalArgumentException is the latitude value is illegal
    ///
    /// @param latitude A value from (-90 to 90)
    public static void checkLatitude(double latitude) {
        /*
         * Note: do not use Preconditions.checkArgument, it is significantly slower because it
         * creates a unique error String every call (and that can be significant if this data check
         * occurs inside a tight loop)
         */
        if (!(-90.0 <= latitude && latitude <= 90)) {
            throw new IllegalArgumentException("Latitude is out of range: " + latitude);
        }
    }

    /// Throw an IllegalArgumentException is the longitude value is illegal
    ///
    /// @param longitude A value from (-180 to 180)
    public static void checkLongitude(double longitude) {
        /*
         * Note: do not use Preconditions.checkArgument, it is significantly slower because it
         * creates a unique error String every call (and that can be significant if this data check
         * occurs inside a tight loop)
         */
        if (!(-180.0 <= longitude && longitude <= 180.0)) {
            throw new IllegalArgumentException("Longitude is out of range: " + longitude);
        }
    }

    /// Clamps the provided latitude value to the closed range [-90,90]
    ///
    /// @param latitude A candidate latitude value that could be outside the legal range
    /// @return Math.min(90.0, Math.max (latitude, - 90.0));
    public static double clampLatitude(double latitude) {
        return Math.min(90.0, Math.max(latitude, -90.0));
    }

    /// Clamps the provided longitude value to the closed range [-180,180]
    ///
    /// @param longitude A candidate longitude value that could be outside the legal range
    /// @return Math.min(180.0, Math.max (longitude, - 180.0));
    public static double clampLongitude(double longitude) {
        return Math.min(180, Math.max(longitude, -180.0));
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    @Override
    @NonNull
    public String toString() {
        return "(" + latitude + "," + longitude + ")";
    }

    /// @return This LatLong as a byte[] of length 16 containing the 8-byte doubles latitude and longitude.
    public byte[] toBytes() {
        return ByteBuffer.allocate(16).putDouble(latitude).putDouble(longitude).array();
    }

    /// @return The Base64 file and url safe encoding of this LatLong's byte[] .
    public String toBase64() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(toBytes());
    }

    /// @return a LatLong version of this LatLong. Saves 50% space, while losing accuracy at 7th decimal place.
    public LatLong compress() {
        return LatLong.fromLatLong(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Long.hashCode(Double.doubleToLongBits(this.latitude));
        hash = 89 * hash + Long.hashCode(Double.doubleToLongBits(this.longitude));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LatLong128 other = (LatLong128) obj;
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        }
        if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LatLong128 other) {
        return ComparisonChain.start()
                .compare(latitude, other.latitude)
                .compare(longitude, other.longitude)
                .result();
    }
}

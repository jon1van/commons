package io.github.jon1van.commons.units;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.Base64;

import com.google.common.collect.ComparisonChain;


/// LatLong is an Immutable, lossy-compressed representation of a Latitude Longitude pair.
///
/// The compression used here saves 50% of the space while maintaining numeric equivalence for the first 7 decimal
/// places. This class uses two 32-bit ints to encode latitude and longitude values (as opposed to
/// directly storing the values in two 64-bit doubles). This space savings is relevant when you store
/// many latitude and longitude pairs as seen in [LatLongPath].
///
/// Here is an example of the small accuracy concession made when converting two doubles to a
/// LatLong. "LatLong128.of(20*PI, -10*PI)" stores the 2 double primitives: (62.83185307179586,
/// -31.41592653589793). Whereas "LatLong.of(20*PI, -10*PI)" stores 2 ints that equate to the
/// values: (62.8318531, -31.4159265). Notice, these approximate values are perfect to the 7th
/// decimal place. Geo-Location data is difficult and expensive to measure beyond this level of
/// accuracy.
///
/// LatLong purposefully does not implement java.io.Serializable. Instead, this class provides 3
/// byte-efficient encodings: as a primitive long, as a byte[], and as a Base64 encoded String. If
/// you absolutely require a Serializable type replace references to LatLong with one of these
/// encodings.
public class LatLong implements HasLatLong, Comparable<LatLong> {

    // The compression technique used here is inspired by how ASTERIX stores values in 32 bits.
    // ASTERIX's technique SUPPORTS MORE UNIQUE LAT_LONG VALUES, but these values are not "aligned
    // nicely" on the decimal grid. Therefore, NO encoded value perfectly matches a "nice input"
    // LatLong of say (1.234, -5.678). The ASTERIX technique ALWAYS INJECTS UGLY NUMERIC ERROR that
    // converts inputs like (1.234, -5.678) to something like (1.2339998314519, -5.678000410343).

    // To prevent, the "aesthetics" issue of ASTERIX, but get equal space savings, we use the
    // technique often used to store currency amounts (e.g. store NUMBER_OF_CENTS as an int
    // instead of storing NUM_OF_DOLLARS as a double).

    // A Java int can hold values from -2,147,483,648 to 2,147,483,647 -- E.g. 4.29 Billion possible values.
    //
    // Thus, we can store 180.0 as 1_800_000_000 and -180.0 as -1_800_000_000.
    // This means we can EXACTLY store all possible Latitude or Longitude values when we limit
    // the inputs to 7 digits of accuracy. E.g. We can store double: 179.123_456_7XX_XXX as the
    // primitive int 1_791_234_567.

    private final int latitudeAsInt;
    private final int longitudeAsInt;

    static int encodeAsInt(double latOrLong) {
        double shifted = latOrLong * 10_000_000.0;
        // Do not simply cast, Round!.
        // This prevents input latOrLong values like "1.001" becoming "10_009_999" (e.g., 1.0009999)
        return (int) Math.round(shifted);
    }

    static double decodeInt(int latOrLongAsInt) {
        return ((double) latOrLongAsInt) / 10_000_000.0;
    }

    /// All public construction requires using LatLong's compress() method.
    public LatLong(Double latitude, Double longitude) {
        // go through LatLong to get bounds checking
        this(LatLong128.of(latitude, longitude));
    }

    /// Create a new LatLong object.
    ///
    /// @param latitude  A non-null Latitude value from (-90 to 90)
    /// @param longitude A non-null Longitude value from (-180 to 180)
    ///
    /// @return A newly created LatLong object (this is a lossy compression)
    public static LatLong of(Double latitude, Double longitude) {
        return new LatLong(latitude, longitude);
    }


    /// All public construction requires using LatLong's compress() method.
    LatLong(LatLong128 location) {
        this(encodeAsInt(location.latitude()), encodeAsInt(location.longitude()));
    }

    private LatLong(int encodedLatitude, int encodedLongitude) {
        // Ensures deserializing byte[] and long inputs are always checked
        checkArgument(-900000000 <= encodedLatitude && encodedLatitude <= 900000000);
        checkArgument(-1800000000 <= encodedLongitude && encodedLongitude <= 1800000000);
        this.latitudeAsInt = encodedLatitude;
        this.longitudeAsInt = encodedLongitude;
    }

    public static LatLong fromLatLong(LatLong128 loc) {
        return new LatLong(loc.latitude(), loc.longitude());
    }

    /// Parse a LatLong from a 64-bit primitive long.
    ///
    /// @param LatLongBits The "latitudeAsInt" is in the upper 32 bits and the "longitudeAsInt" is
    ///                      in the lower 32 bits.
    ///
    /// @return A new created LatLong object
    public static LatLong fromPrimitiveLong(long LatLongBits) {

        int lngBits = (int) LatLongBits;
        int latBits = (int) (LatLongBits >> 32);

        return new LatLong(latBits, lngBits);
    }

    /// Create a new LatLong object.
    ///
    /// @param exactly8Bytes The bytes defining two ints: {latitudeAsInt, longitudeAsInt}
    ///
    /// @return A new LatLong object.
    public static LatLong fromBytes(byte[] exactly8Bytes) {
        requireNonNull(exactly8Bytes);
        checkArgument(exactly8Bytes.length == 8, "Must use exactly 8 bytes");
        ByteBuffer buffer = ByteBuffer.wrap(exactly8Bytes);

        int latitudeAsInt = buffer.getInt();
        int longitudeAsInt = buffer.getInt();
        return new LatLong(latitudeAsInt, longitudeAsInt);
    }

    /// Create a new LatLong object.
    ///
    /// @param base64Encoding The Base64 safe and URL safe (no padding) encoding of a LatLong's
    ///                       byte[]
    ///
    /// @return A new LatLong object.
    public static LatLong fromBase64Str(String base64Encoding) {
        return LatLong.fromBytes(Base64.getUrlDecoder().decode(base64Encoding));
    }

    /// Convert this compressed LatLong into a LatLong. This provides access to the "distance and
    /// direction" functions that LatLong supports but LatLong does not.
    public LatLong128 inflate() {
        return LatLong128.of(latitude(), longitude());
    }


    public double latitude() {
        return decodeInt(latitudeAsInt);
    }

    public double longitude() {
        return decodeInt(longitudeAsInt);
    }

    /// @return This LatLong written with 7 digits after the decimal point. Six digits are shown
    ///     because all additional digits are unreliable due to the lossy compression.
    @Override
    public String toString() {
        return "(" + String.format("%.7f", latitude()) + "," + String.format("%.7f", longitude()) + ")";
    }

    /// @return This LatLong as a 64-bit long (built by bit packing 2 32-bit int values).
    public long toPrimitiveLong() {
        return pack(latitudeAsInt, longitudeAsInt);
    }

    /// Combine the bits from two 32-bit int primitives into a single 64 bit long.
    static long pack(int upperInt, int lowerInt) {

        long upperBits = ((long) upperInt) << 32;
        long lowerBits = lowerInt & 0xffffffffL;
        // See also: "Integer.toUnsignedLong(int)" -- Using bitwise & to handle negative integers

        return upperBits | lowerBits;
    }

    /// @return This LatLong as a byte[] of length 8. The array can be interpreted as a single
    ///     8-byte long OR 2 4-byte ints that contain the "int encoded" latitude and longitude
    ///     values. These 2 encodings are equivalent.
    public byte[] toBytes() {
        return ByteBuffer.allocate(8).putLong(toPrimitiveLong()).array();
        // SAME AS
        // ByteBuffer.allocate(8).putInt(latitudeAsInt).putInt(longitudeAsInt).array();
    }

    /// @return An 11 character Base64 file and url safe encoding of this LatLong's byte[] (e.g.,
    ///     "KUJSEZn8uzs", "-NWDIbs8BTQ", or "aHpvnvpRj8Y")
    public String toBase64() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(toBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LatLong LatLong = (LatLong) o;

        if (latitudeAsInt != LatLong.latitudeAsInt) return false;
        return longitudeAsInt == LatLong.longitudeAsInt;
    }

    @Override
    public int hashCode() {
        int result = latitudeAsInt;
        result = 31 * result + longitudeAsInt;
        return result;
    }

    @Override
    public int compareTo(LatLong other) {
        return ComparisonChain.start()
                .compare(latitudeAsInt, other.latitudeAsInt)
                .compare(longitudeAsInt, other.longitudeAsInt)
                .result();
    }
}

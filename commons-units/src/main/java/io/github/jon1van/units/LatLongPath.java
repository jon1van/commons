package io.github.jon1van.units;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

/// This class provides a byte-efficient way to store many latitude and longitude pairs.
///
/// The core usage idiom of this class is:
/// ```java
/// LatLongPath myPath = LatLongPath.from(oneThousandLatLongs);
/// byte[] pathAsByte = myPath.toBytes(); //8_000 bytes!`
/// ```
/// `
/// LatLongPath purposefully does not implement java.io.Serializable. Instead, this class provides
/// 2 different byte-efficient encodings: as a byte[], and as a Base64 encoded String. If you strictly
/// require a Serializable type replace references to LatLongPath with one of these encodings.
public class LatLongPath implements Iterable<LatLong> {

    /// Get the Encoder exactly once.
    private static final Base64.Encoder BASE_64_ENCODER = Base64.getUrlEncoder().withoutPadding();

    /// This array contains {latLong64_0, latLong64_1, latLong64_2, ...}.
    private final long[] locationData;

    /// Build a LatLongPath by iterating through these locations.
    public LatLongPath(Collection<LatLong> locations) {
        requireNonNull(locations);

        this.locationData = new long[locations.size()];
        Iterator<LatLong> iter = locations.iterator();

        int i = 0;
        while (iter.hasNext()) {
            LatLong loc = iter.next();
            locationData[i] = loc.toPrimitiveLong();
            i++;
        }
    }

    /// Build a LatLongPath by iterating through these locations.
    public LatLongPath(LatLong... locations) {
        requireNonNull(locations);
        this.locationData = new long[locations.length];
        for (int i = 0; i < locations.length; i++) {
            locationData[i] = locations[i].toPrimitiveLong();
        }
    }

    private LatLongPath(long[] data) {
        requireNonNull(data);
        this.locationData = data;
    }

    /// Build a LatLong64Path by iterating through these locations.
    public static LatLongPath from(Collection<LatLong> locations) {
        return new LatLongPath(locations);
    }

    /// Build a LatLong64Path by iterating through these locations.
    public static LatLongPath from(LatLong... locations) {
        return new LatLongPath(locations);
    }

    /// Build a LatLongPath by iterating through these locations.
    public static LatLongPath from(LatLongPath path) {
        return new LatLongPath(path.toArray());
    }

    /// Create a new LatLongPath from an array of bytes that looks like: {latLong64_0, latLong64_1,
    /// latLong64_2, ...} (each LatLong64 is encoded as one 8-byte long)
    public static LatLongPath fromBytes(byte[] bytes) {
        requireNonNull(bytes);
        checkArgument(bytes.length % 8 == 0, "The byte[] must have a multiple of 8 bytes");

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long[] latLongData = new long[bytes.length / 8];
        for (int i = 0; i < latLongData.length; i++) {
            latLongData[i] = buffer.getLong();
        }

        return new LatLongPath(latLongData);
    }

    /// Create a new LatLongPath object.
    ///
    /// @param base64Encoding The Base64 safe and URL safe (no padding) encoding of a LatLongPath's
    ///                       byte[]
    ///
    /// @return A new LatLongPath object.
    public static LatLongPath fromBase64Str(String base64Encoding) {
        return LatLongPath.fromBytes(Base64.getUrlDecoder().decode(base64Encoding));
    }

    /// Returns a LatLongPath that is a subset of this LatLongPath.  This method has the same
    /// semantics as `String.substring(int beginIndex, int endIndex)`
    ///
    /// @param beginIndex the beginning index, inclusive.
    /// @param endIndex   the ending index, exclusive.
    ///
    /// @return The specified LatLongPath.
    public LatLongPath subpath(int beginIndex, int endIndex) {
        checkArgument(beginIndex >= 0, "beginIndex cannot be negative");
        checkArgument(endIndex <= size(), "endIndex cannot be greater than size()");
        checkArgument(beginIndex <= endIndex, "endIndex must be >= beginIndex");

        int len = endIndex - beginIndex;
        long[] subset = new long[len];
        System.arraycopy(locationData, beginIndex, subset, 0, len);

        return new LatLongPath(subset);
    }

    /// @return This LatLongPath as a byte[] containing 8 bytes per LatLong64 in the path
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(8 * size());
        for (long locationAsLong : locationData) {
            buffer.putLong(locationAsLong);
        }
        return buffer.array();
    }

    /// @return The Base64 file and url safe encoding of this LatLongPath's byte[] .
    public String toBase64() {
        return BASE_64_ENCODER.encodeToString(toBytes());
    }

    public Stream<LatLong> stream() {
        return toList().stream();
    }

    public ArrayList<LatLong> toList() {
        ArrayList<LatLong> list = new ArrayList<>(size());
        for (long locationDatum : locationData) {
            list.add(LatLong.fromPrimitiveLong(locationDatum));
        }

        return list;
    }

    public LatLong[] toArray() {

        LatLong[] array = new LatLong[locationData.length];
        for (int i = 0; i < locationData.length; i++) {
            array[i] = LatLong.fromPrimitiveLong(locationData[i]);
        }
        return array;
    }

    /// @return The i_th entry in this path (yields same result as this.asList().get(i)).
    public LatLong get(int i) {
        checkArgument(0 <= i && i < locationData.length);
        return LatLong.fromPrimitiveLong(locationData[i]);
    }

    /// The number of LatLong locations in this path.
    public int size() {
        return locationData.length;
    }

    public boolean isEmpty() {
        return locationData.length == 0;
    }

    @Override
    @NonNull
    public Iterator<LatLong> iterator() {
        return toList().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        LatLongPath latLongs = (LatLongPath) o;
        return Arrays.equals(locationData, latLongs.locationData);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(locationData);
    }

    /// Compute the "total distance" between the points in these two paths.
    ///
    /// The distance computed here is the sum of the distances between "LatLong pairs" taken from the
    /// two paths (e.g. the distance btw the 1st LatLong from both paths PLUS the distance btw the
    /// 2nd LatLong from both paths PLUS the distance btw the 3rd LatLong from both paths ...).
    ///
    /// The "distanceBtw" between identical paths will be 0. The "distanceBtw" between nearly
    /// identical paths will be small.  The "distanceBtw" between two very different paths will be
    /// large.
    ///
    /// The computation requires both Paths to have the same size. This is an important requirement
    /// for making a DistanceMetric using this method.
    ///
    /// @param p1 A path
    /// @param p2 Another path
    ///
    /// @return The sum of the pair-wise distance measurements
    public static double distanceBtw(LatLongPath p1, LatLongPath p2) {
        // ACCURATE BUT SLOW
        requireNonNull(p1);
        requireNonNull(p2);
        checkArgument(p1.size() == p2.size(), "Paths must have same size");

        double distanceSum = 0;
        int n = p1.size();

        for (int i = 0; i < n; i += 1) {
            LatLong mine = p1.get(i);
            LatLong his = p2.get(i);
            distanceSum += mine.distanceInNmTo(his);
        }

        // return the distance
        return distanceSum;
    }

    /// Compute the "total distance" between the first n points of these two paths.
    ///
    /// The distance computed here is the sum of the distances between "LatLong pairs" taken from the
    /// two paths (e.g. the distance btw the 1st LatLong from both paths PLUS the distance btw the
    /// 2nd LatLong from both paths PLUS the distance btw the 3rd LatLong from both paths ...).
    ///
    /// The "distanceBtw" between two identical paths will be 0. The "distanceBtw" between two nearly
    /// identical paths will be small. The "distanceBtw" between two very different paths will be
    /// large.
    ///
    /// This The computation requires both Paths to have the same size.  This is an important
    /// requirement for making a DistanceMetric using this method.
    ///
    /// @param p1 A path
    /// @param p2 Another path
    /// @param n  The number of points considered in the "path distance" computation
    ///
    /// @return The sum of the pair-wise distance measurements
    public static double distanceBtw(LatLongPath p1, LatLongPath p2, int n) {
        // ACCURATE BUT SLOW
        requireNonNull(p1);
        requireNonNull(p2);
        checkArgument(n >= 0);
        checkArgument(p1.size() >= n, "Path1 does not have the required length");
        checkArgument(p2.size() >= n, "Path2 does not have the required length");

        double distanceSum = 0;
        for (int i = 0; i < n; i += 1) {
            LatLong mine = p1.get(i);
            LatLong his = p2.get(i);
            distanceSum += mine.distanceInNmTo(his);
        }

        // return the distance
        return distanceSum;
    }
}

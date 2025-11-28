package io.github.jon1van.units;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class LatLongPathTest {

    @Test
    void demonstrate_core_usage_pattern() {

        List<LatLong> manyLocations = randomLatLongs(1_000);
        LatLongPath path = LatLongPath.from(manyLocations);

        byte[] uncompressedBytes = path.toBytes();

        // 8k bytes for 1k LatLongs
        assertThat(uncompressedBytes.length).isEqualTo(1_000 * 8);
    }

    static List<LatLong> randomLatLongs(int n) {
        Random rng = new Random(17L);

        List<LatLong> latLongs = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {

            double lat = rng.nextDouble() * 90.0;
            double lng = rng.nextDouble() * 180.0;
            if (rng.nextBoolean()) {
                lat *= -1.0;
            }
            if (rng.nextBoolean()) {
                lng *= -1.0;
            }
            latLongs.add(LatLong.of(lat, lng));
        }

        return latLongs;
    }

    @Test
    void basicConstructor() {

        LatLongPath path = LatLongPath.from(LatLong.of(0.0, 0.1), LatLong.of(1.0, 1.1));

        assertThat(path.get(0)).isEqualTo(LatLong.of(0.0, 0.1));
        assertThat(path.get(1)).isEqualTo(LatLong.of(1.0, 1.1));
        assertThat(path.size()).isEqualTo(2);
    }

    @Test
    void basicConstructor_collectionBased() {

        ArrayList<LatLong> list = new ArrayList<>();
        list.add(LatLong.of(0.0, 0.1));
        list.add(LatLong.of(1.0, 1.1));

        LatLongPath path = LatLongPath.from(list);

        assertThat(path.get(0)).isEqualTo(LatLong.of(0.0, 0.1));
        assertThat(path.get(1)).isEqualTo(LatLong.of(1.0, 1.1));
        assertThat(path.size()).isEqualTo(2);
    }

    @Test
    void constructorEquivalence() {

        LatLong[] array = new LatLong[] {LatLong.of(0.0, 0.1), LatLong.of(1.0, 1.1)};
        ArrayList<LatLong> list = new ArrayList<>();
        list.add(LatLong.of(0.0, 0.1));
        list.add(LatLong.of(1.0, 1.1));

        LatLongPath path1 = LatLongPath.from(array);
        LatLongPath path2 = LatLongPath.from(list);

        assertThat(path1).isEqualTo(path2);
        assertThat(path1.size()).isEqualTo(2);
        assertThat(path1.size()).isEqualTo(path2.size());
        assertThat(path1.toBase64()).isEqualTo(path2.toBase64());
    }

    @Test
    public void toAndFromBytes() {

        LatLongPath path = LatLongPath.from(LatLong.of(0.0, 0.1), LatLong.of(1.0, 1.1), LatLong.of(2.0, 2.1));
        LatLongPath path2 = LatLongPath.fromBytes(path.toBytes());

        assertThat(path.size()).isEqualTo(path2.size());
        for (int i = 0; i < path.size(); i++) {
            assertThat(path.get(i)).isEqualTo(path2.get(i));
        }

        assertThat(path.equals(path2)).isTrue();
    }

    @Test
    void toBytes_yields_8_bytes_per_location() {

        LatLongPath path = LatLongPath.from(LatLong.of(0.0, 0.1), LatLong.of(1.0, 1.1), LatLong.of(2.0, 2.1));

        byte[] asBytes = path.toBytes();

        assertThat(asBytes.length).isEqualTo(3 * 8);
    }

    @Test
    public void toAndFromBase64() {

        LatLongPath path = LatLongPath.from(LatLong.of(0.0, 0.1), LatLong.of(1.0, 1.1), LatLong.of(2.0, 2.1));
        LatLongPath path2 = LatLongPath.fromBase64Str(path.toBase64());

        assertThat(path.size()).isEqualTo(path2.size());
        for (int i = 0; i < path.size(); i++) {
            assertThat(path.get(i)).isEqualTo(path2.get(i));
        }

        assertThat(path.equals(path2)).isTrue();
    }

    @Test
    public void supportEmptyPaths() {

        LatLongPath path = LatLongPath.from();

        assertThat(path.size()).isEqualTo(0);
        assertThat(path.isEmpty()).isTrue();
        assertThat(path.toArray()).isEqualTo(new LatLong[] {});
        assertThat(path.toList().size()).isEqualTo(0);

        byte[] bytes = path.toBytes();
        LatLongPath path2 = LatLongPath.fromBytes(bytes);

        assertThat(path).isEqualTo(path2);
    }

    @Test
    public void testSubpath() {

        LatLong a = LatLong.of(0.0, 0.1);
        LatLong b = LatLong.of(1.0, 1.1);
        LatLong c = LatLong.of(2.0, 2.1);

        LatLongPath fullPath = LatLongPath.from(a, b, c);

        // full "copy subset" gives unique object with same data
        LatLongPath abc = fullPath.subpath(0, 3);
        assertThat(fullPath.equals(abc)).isTrue();
        assertThat(fullPath == abc).isFalse();

        assertThat(fullPath.subpath(0, 0)).isEqualTo(LatLongPath.from());
        assertThat(fullPath.subpath(0, 1)).isEqualTo(LatLongPath.from(a));
        assertThat(fullPath.subpath(0, 2)).isEqualTo(LatLongPath.from(a, b));
        assertThat(fullPath.subpath(0, 3)).isEqualTo(LatLongPath.from(a, b, c));

        assertThat(fullPath.subpath(1, 1)).isEqualTo(LatLongPath.from());
        assertThat(fullPath.subpath(1, 2)).isEqualTo(LatLongPath.from(b));
        assertThat(fullPath.subpath(1, 3)).isEqualTo(LatLongPath.from(b, c));

        assertThat(fullPath.subpath(2, 3)).isEqualTo(LatLongPath.from(c));

        assertThrows(IllegalArgumentException.class, () -> fullPath.subpath(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> fullPath.subpath(3, 1));
        assertThrows(IllegalArgumentException.class, () -> fullPath.subpath(0, 4));
    }

    @Test
    void canParseBase64String() {
        // This path was pulled from the MB-Tree project, where we encode track snippets as LatLong paths.
        String pathString =
                "E8Fg68ZiPmgTwUAoxmIXFxPBIC7GYfEPE8EBxMZhyj4TwORKxmGi8RPAw4fGYX2OE8CjLMZhV3kTwIPixmEvpxPAY6PGYQjdE8BD6cZg4MQTwCScxmC3NxPAA8_GYI0uE7_j1cZgYTsTv8TzxmA1zhO_p9bGYAvWE7-LOsZf42YTv23rxl-7shO_UDnGX5RoE78yKMZfbmQTvxVvxl9HARO--23GXxyzE77mOsZe7TwTvthVxl637hO-0irGXnuEE77VB8ZeOTsTvtg1xl392RO-3ArGXcKHE77h7sZdhw0Tvua2xl1LTxO-69fGXQ7HE77yQsZc0AcTvvqJxlyO_xO_BavGXEy9E78V5MZcCx8TvyQVxlvI5RO_MMbGW4ZpE782gcZbRfITvz24xlsEgBO_RZjGWsIcE79NVcZafrATv1Ocxlo5CA";

        LatLongPath path = LatLongPath.fromBase64Str(pathString);

        assertThat(path.size()).isEqualTo(41);
    }

    static void assertPathsAreBasicallyTheSame(LatLongPath path1, LatLongPath path2) {

        assertThat(path1.size()).isEqualTo(path2.size());

        for (int i = 0; i < path1.size(); i++) {
            LatLong path1_i = path1.get(i);
            LatLong path2_i = path2.get(i);
            assertThat(path1_i.latitude()).isEqualTo(path2_i.latitude(), within(0.000_000_1));
            assertThat(path1_i.longitude()).isEqualTo(path2_i.longitude(), within(0.000_000_1));
        }
    }

    @Test
    void pathDistanceIsCorrect() {

        LatLong a = LatLong.of(0.0, 0.1);
        LatLong b = LatLong.of(1.0, 1.1);
        LatLong c = LatLong.of(2.0, 2.1);

        LatLongPath fullPath = LatLongPath.from(a, b, c);
        Distance sum = a.distanceTo(b).plus(b.distanceTo(c));

        assertThat(fullPath.pathDistance()).isEqualTo(sum);
    }
}

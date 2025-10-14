package io.github.jon1van.commons.units;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static io.github.jon1van.commons.units.LatLong.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LatLongTest {

    @Test
    void encoding_LatLong_doubles_as_ints() {

        assertThat(encodeAsInt(0.0)).isEqualTo(0);
        assertThat(encodeAsInt(0.001)).isEqualTo(10_000);
        assertThat(encodeAsInt(1.001)).isEqualTo(10_010_000);
        assertThat(encodeAsInt(123.456789)).isEqualTo(1_234_567_890);
        assertThat(encodeAsInt(123.4567891)).isEqualTo(1_234_567_891);
        assertThat(encodeAsInt(180.0)).isEqualTo(1_800_000_000);

        assertThat(decodeInt(1_234_567_891)).isEqualTo(123.4567891);
        assertThat(decodeInt(1_234_567_890)).isEqualTo(123.456789);
        assertThat(decodeInt(10_010_000)).isEqualTo(1.001);
        assertThat(decodeInt(10_000)).isEqualTo(0.001);
        assertThat(decodeInt(0)).isEqualTo(0.0);

        assertThat(encodeAsInt(-0.0)).isEqualTo(0);
        assertThat(encodeAsInt(-0.001)).isEqualTo(-10_000);
        assertThat(encodeAsInt(-1.001)).isEqualTo(-10_010_000);
        assertThat(encodeAsInt(-123.456789)).isEqualTo(-1_234_567_890);
        assertThat(encodeAsInt(-123.4567891)).isEqualTo(-1_234_567_891);

        assertThat(decodeInt(-1_234_567_891)).isEqualTo(-123.4567891);
        assertThat(decodeInt(-1_234_567_890)).isEqualTo(-123.456789);
        assertThat(decodeInt(-10_010_000)).isEqualTo(-1.001);
        assertThat(decodeInt(-10_000)).isEqualTo(-0.001);
        assertThat(decodeInt(0)).isEqualTo(0.0);
    }

    @Test
    void lossy_but_very_accurate_approximation() {

        double lat = 20 * Math.PI; // 62.831853071795865
        double lng = -10 * Math.PI; // -31.415926535897932

        LatLong location = new LatLong(lat, lng);

        // Not the Same!
        assertThat(location.latitude() == lat).isFalse(); // 62.831853033430804
        assertThat(location.longitude() == lng).isFalse(); // -31.415926474805886

        // But extremely Similar!
        double TOL = 1E-7;
        assertThat(location.latitude()).isCloseTo(lat, within(TOL));
        assertThat(location.longitude()).isCloseTo(lng, within(TOL));
    }

    @Test
    void will_reject_invalid_int_encodings() {

        int illegal_lat_hi = encodeAsInt(90.0000001);
        int illegal_lat_low = encodeAsInt(-90.0000001);

        System.out.println(illegal_lat_hi);
        System.out.println(illegal_lat_low);

        assertThrows(IllegalArgumentException.class, () -> LatLong.fromPrimitiveLong(pack(illegal_lat_hi, 0)));

        assertThrows(IllegalArgumentException.class, () -> LatLong.fromPrimitiveLong(pack(illegal_lat_low, 0)));

        int illegal_long_hi = encodeAsInt(180.0000001);
        int illegal_long_low = encodeAsInt(-180.0000001);

        assertThrows(IllegalArgumentException.class, () -> LatLong.fromPrimitiveLong(pack(0, illegal_long_hi)));

        assertThrows(IllegalArgumentException.class, () -> LatLong.fromPrimitiveLong(pack(0, illegal_long_low)));
    }

    @Test
    void randomized_lossy_accuracy_verification() {

        int NUM_TRIALS = 10_000;
        double TOLERANCE = 1E-7;
        Random rng = new Random(17L);

        for (int i = 0; i < NUM_TRIALS; i++) {
            LatLong128 loc = randomLatLong(rng);

            LatLong lossy_location = new LatLong(loc.latitude(), loc.longitude());
            assertThat(lossy_location.latitude()).isCloseTo(loc.latitude(), within(TOLERANCE));
            assertThat(lossy_location.longitude()).isCloseTo(loc.longitude(), within(TOLERANCE));
        }
    }

    @Test
    void toBytes_and_back() {

        LatLong instance = new LatLong(15.0, 22.0);

        byte[] asBytes = instance.toBytes();

        LatLong instanceRemake = LatLong.fromBytes(asBytes);

        assertThat(instance).isEqualTo(instanceRemake);
        assertThat(instance.latitude()).isEqualTo(instanceRemake.latitude());
        assertThat(instance.longitude()).isEqualTo(instanceRemake.longitude());
    }

    @Test
    void toBase64_and_back() {

        Random rng = new Random(17L);
        int N = 50;

        for (int i = 0; i < N; i++) {
            LatLong128 loc = randomLatLong(rng);
            LatLong in = loc.compress();
            String asBase64 = in.toBase64();
            LatLong out = LatLong.fromBase64Str(asBase64);

            assertThat(in).isEqualTo(out);
            assertThat(in.latitude()).isEqualTo(out.latitude());
            assertThat(in.longitude()).isEqualTo(out.longitude());
        }
    }

    @Test
    void fromBase64Str_example() {
        // provides and example of decoding a base64 String AND breaks if encoding strategy changes
        String base64Str = "DMu9QwSdrGw";
        LatLong location = LatLong.fromBase64Str(base64Str);
        assertThat(location.toString()).isEqualTo("(21.4678851,7.7442156)");
    }

    @Test
    void toString_shows_7_digits() {
        LatLong instance = new LatLong(76.123, -23.201);

        String expectedStr = "(76.1230000,-23.2010000)";

        assertThat(instance.toString()).isEqualTo(expectedStr);
    }

    @Test
    void toPrimitiveLongAndBack() {
        Random rng = new Random(17L);
        int N = 50;

        for (int i = 0; i < N; i++) {
            LatLong128 random = randomLatLong(rng);
            LatLong instance = new LatLong(random.latitude(), random.longitude());

            long asLong = instance.toPrimitiveLong();

            LatLong instance2 = LatLong.fromPrimitiveLong(asLong);

            assertThat(instance.latitude()).isEqualTo(instance2.latitude());
            assertThat(instance.longitude()).isEqualTo(instance2.longitude());
            assertThat(instance).isEqualTo(instance2);
        }
    }

    static LatLong128 randomLatLong(Random rng) {
        double lat = rng.nextDouble() * 90.0;
        double lng = rng.nextDouble() * 180.0;
        if (rng.nextBoolean()) {
            lat *= -1.0;
        }
        if (rng.nextBoolean()) {
            lng *= -1.0;
        }
        return LatLong128.of(lat, lng);
    }
}

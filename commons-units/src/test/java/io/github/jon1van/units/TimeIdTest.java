package io.github.jon1van.units;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.units.TimeId.*;
import static java.lang.Math.sqrt;
import static java.time.Instant.EPOCH;
import static java.time.Instant.now;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.math.StatsAccumulator;
import org.junit.jupiter.api.Test;

class TimeIdTest {

    @Test
    public void constructorEmbedsTime() {
        Instant now = now();

        TimeId id = new TimeId(EPOCH.minusMillis(1L));
        TimeId id_1 = new TimeId(EPOCH.plusMillis(1L));
        TimeId idNow = new TimeId(now);

        assertThat(id.time()).isEqualTo(EPOCH.minusMillis(1L));
        assertThat(id.timeAsEpochMs()).isEqualTo(-1L);

        assertThat(id_1.time()).isEqualTo(EPOCH.plusMillis(1L));
        assertThat(id_1.timeAsEpochMs()).isEqualTo(1L);

        // The "epochMilliSeconds" are the same
        assertThat(idNow.timeAsEpochMs()).isEqualTo(now.toEpochMilli());

        // But! We CANNOT correctly make this assertion because the nanoseconds may or may not be the same
        //  It will depend on the system clock used within Instant.now()
        // assertThat(idNow.time(), is(not(now)));
    }

    @Test
    public void fromStringParserYieldsEquivalentIds() {

        Instant now = now();

        TimeId id = new TimeId(EPOCH);
        TimeId id_1 = new TimeId(EPOCH.plusMillis(1L));
        TimeId idNow = new TimeId(now);

        assertThat(TimeId.fromString(id.toString())).isEqualTo(id);
        assertThat(TimeId.fromString(id_1.toString())).isEqualTo(id_1);
        assertThat(TimeId.fromString(idNow.toString())).isEqualTo(idNow);
    }

    @Test
    public void bytesMethodAndConstructorAreConsistent() {

        Instant now = now();

        TimeId idNow = new TimeId(now); // use the most straight forward constructor
        byte[] byteEncoding = idNow.bytes();
        TimeId fromBytes = new TimeId(byteEncoding); // build a new ID directly from the bytes

        assertThat(idNow.time()).isEqualTo(fromBytes.time());
        assertArrayEquals(idNow.bytes(), fromBytes.bytes());
    }

    @Test
    public void base64Encoding() {

        Instant now = now();

        TimeId idNow = new TimeId(now); // use the most straight forward constructor

        TimeId fromBase64Str = TimeId.fromBase64(idNow.asBase64());

        assertThat(idNow).isEqualTo(fromBase64Str);
        assertThat(idNow.time()).isEqualTo(fromBase64Str.time());
        assertArrayEquals(idNow.bytes(), fromBase64Str.bytes());
    }

    @Test
    public void bulkToStringFromStringCycles() {

        int n = 100;
        for (int i = 0; i < n; i++) {
            TimeId id = newId();
            TimeId rebuilt = TimeId.fromString(id.toString());

            assertThat(id).isEqualTo(rebuilt);
        }
    }

    @Test
    public void base64Encoding_22charsLong() {
        TimeId id = newId();
        assertThat(id.asBase64().length()).isEqualTo(22);
    }

    @Test
    public void base64Encoding_first7CharAreTime() {
        Instant time = now();
        TimeId id1 = newIdFor(time);
        TimeId id2 = newIdFor(time);
        TimeId id3 = newIdFor(time);

        // The 1st 7-char of each base64 encoding contain "Time info" THEREFORE, when the time is the same the chars are
        // the same.
        assertThat(id1.asBase64().substring(0, 7)).isEqualTo(id2.asBase64().substring(0, 7));
        assertThat(id2.asBase64().substring(0, 7)).isEqualTo(id3.asBase64().substring(0, 7));
    }

    @Test
    public void orderingMatchesHasTime() {

        TimeId old = new TimeId(EPOCH.minusMillis(1L));
        TimeId lessOld = new TimeId(EPOCH.plusMillis(1L));
        TimeId another = newId();

        ArrayList<TimeId> byIdCompare = newArrayList(old, lessOld, another);
        ArrayList<TimeId> byHasTime = newArrayList(another, old, lessOld);

        Collections.sort(byIdCompare);
        Collections.sort(byHasTime, Time.compareByTime());

        assertThat(byIdCompare.get(0)).isEqualTo(byHasTime.get(0));
        assertThat(byIdCompare.get(1)).isEqualTo(byHasTime.get(1));
        assertThat(byIdCompare.get(2)).isEqualTo(byHasTime.get(2));
    }

    @Test
    public void cyclicalLongParsing() {
        /*
         * This test isolates the hex parsing component of "toHexString()" and "fromHexString(String)"
         * The implementation was harder than you'd expect because the java.lang.Long parser
         * wasn't working for 64 random bits (when those bits defined a negative long)
         */
        SecureRandom rng = new SecureRandom();

        for (int i = 0; i < 100; i++) {
            long randomLong = rng.nextLong();

            String encoding = String.format("%016x", randomLong);
            long decoding = (new BigInteger(encoding, 16)).longValue();

            assertThat(randomLong).isEqualTo(decoding);
        }
    }

    @Test
    public void toHexStringEncodingAndParsing() {

        Instant now = now();
        TimeId id = new TimeId(now);

        // e.g. "6299c83dbbf26ab8f01257782fb49a37"
        String hexString = id.asHexString();

        assertThat(hexString.length()).isEqualTo(32);

        TimeId rebuiltFromHexStr = TimeId.fromHexString(hexString);

        assertThat(id).isEqualTo(rebuiltFromHexStr);
        assertThat(id.time()).isEqualTo(rebuiltFromHexStr.time());
        assertArrayEquals(id.bytes(), rebuiltFromHexStr.bytes());
    }

    @Test
    public void idsCanBeFileNames() {

        // ALLOW ONLY THESE CHARS:  A-Z, a-z, 0-9, '.', '_', and '-'
        // source  https://en.wikipedia.org/wiki/Filename#Comparison_of_filename_limitations
        // section POSIX "Fully portable filenames

        String REGEX_PATTERN = "^[A-Z a-z 0-9 \\- \\_ \\.]{1,255}$";

        for (int i = 0; i < 100; i++) {
            TimeId id = newId();
            assertThat(id.toString().matches(REGEX_PATTERN)).isTrue();
        }
    }

    @Test
    public void randomBitsAndTimeBitsMakeAllBits() {
        // Show that we can make "id.bytes()" from JUST the time data and JUST the random data
        // e.g., the random bits are complete
        // e.g., id.randomBytes() and id.timeAsEpochMs() contain 100% of the data in the TimeId

        TimeId id = newId();

        byte[] randomBits = id.randomBytes();
        byte[] justTimeBits =
                ByteBuffer.allocate(8).putLong(id.timeAsEpochMs() << 22).array();

        byte[] allBits = id.bytes();

        byte[] manuallyConstructed = new byte[16]; // GOAL -- rebuild "allBits" from randomBits & justTimeBits

        // The "time bits" match the bits we get from "timeId.bytes()"
        for (int j = 0; j < 5; j++) {
            // bits 0-8, 8-16, ... 32-40
            assertThat(justTimeBits[j]).isEqualTo(allBits[j]);
            manuallyConstructed[j] = justTimeBits[j];
        }

        // We can construct the 6th byte (bits 40-48) using timeBits "OR-ed together" with the randomBits
        byte splitByte = (byte) (justTimeBits[5] | randomBits[5]);
        assertThat(splitByte).isEqualTo(allBits[5]);

        manuallyConstructed[5] = splitByte;

        // The "random bits" match the bits we get from "timeId.bytes()"
        for (int j = 6; j < 16; j++) {
            // bits 48-56, 56-64, ... 120-128
            assertThat(randomBits[j]).isEqualTo(allBits[j]);
            manuallyConstructed[j] = randomBits[j];
        }

        TimeId idFromManualBytes = new TimeId(manuallyConstructed);

        assertThat(id).isEqualTo(idFromManualBytes);
    }

    @Test
    public void randomBitsAndEncodingMatch() {

        TimeId id = newId();

        // e.g. "YpmLwbo1MNma0swdxsojUQ"
        String fullBase64Encoding = id.asBase64();

        // e.g. "1MNma0swdxsojUQ"  (
        String rngBase64Encoding = id.rngBitsAsBase64();

        assertThat(fullBase64Encoding.length()).isEqualTo(22);
        assertThat(rngBase64Encoding.length()).isEqualTo(15);
        assertThat(rngBase64Encoding).isEqualTo(id.asBase64().substring(7));

        // e.g. "AAAAAAA1MNma0swdxsojUQ"
        // Manually create the base64 encoding of just the "randomBytes()"
        String base64_fromJustRNG = Base64.getUrlEncoder().withoutPadding().encodeToString(id.randomBytes());

        assertThat(id.rngBitsAsBase64()).isEqualTo(base64_fromJustRNG.substring(7));
    }

    @Test
    public void testAsUniformRand_range0to1_andDistributionIsUniform() {

        // This test will RANDOMLY fail 1 in 15787 tries!

        int SAMPLE_SIZE = 10_000;

        StatsAccumulator accumulator = new StatsAccumulator();
        IntStream.range(0, SAMPLE_SIZE)
                .mapToObj(i -> newId())
                .mapToDouble(id -> id.asUniformRand())
                .forEach(rngSample -> accumulator.add(rngSample));

        // Basic Truths, all sample 0-1, correct number of samples...
        assertThat(accumulator.max()).isLessThan(1.0);
        assertThat(accumulator.min()).isGreaterThan(0.0);
        assertThat((int) accumulator.count()).isEqualTo(SAMPLE_SIZE);

        Double standDev = accumulator.sampleStandardDeviation();

        // SOURCE = https://en.wikipedia.org/wiki/Continuous_uniform_distribution
        double expectedVariance = 1.0 / 12.0;
        double expectedStdDev = sqrt(expectedVariance);
        double expectedMean = .5;

        double stdDev_of_xBar = expectedStdDev / sqrt(SAMPLE_SIZE);

        double zScore = (accumulator.mean() - expectedMean) / stdDev_of_xBar;

        // SOURCE = https://en.wikipedia.org/wiki/68%E2%80%9395%E2%80%9399.7_rule
        // Allowing +- 4 standard deviations or 0.999936657516334 (pass prob)
        //  This test will RANDOMLY fail 1 in 15787 tries
        assertThat(zScore).isEqualTo(0, within(4.0));
    }

    @Test
    public void demo_uniformRand_forRandomSampling() {

        // NOT A UNIT TEST -- Demonstration only...

        int DATA_SET_SIZE = 1000;
        int SAMPLE_SIZE = 20;

        // Make a dataset
        Map<TimeId, Integer> someKeyedData = new HashMap<>();
        IntStream.range(0, DATA_SET_SIZE).forEach(i -> someKeyedData.put(newId(), i));

        // Take a random sample by sorting via the uniform random variable
        List<Integer> RANDOM_SAMPLE = someKeyedData.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().asUniformRand()))
                .limit(SAMPLE_SIZE)
                .map(entry -> entry.getValue())
                .collect(Collectors.toList());
    }

    // exists to make tests more readable...
    private static String inBinary(long val) {
        return Long.toBinaryString(val);
    }

    @Test
    public void bitmasksAreCorrect() {

        long oneBit = makeBitMask(1);
        long twoBits = makeBitMask(2);
        long threeBits = makeBitMask(3);
        long fourBits = makeBitMask(4);

        assertThat(inBinary(oneBit)).isEqualTo("1");
        assertThat(inBinary(twoBits)).isEqualTo("11");
        assertThat(inBinary(threeBits)).isEqualTo("111");
        assertThat(inBinary(fourBits)).isEqualTo("1111");
    }
}

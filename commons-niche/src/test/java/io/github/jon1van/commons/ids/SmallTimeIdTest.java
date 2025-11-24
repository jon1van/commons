package io.github.jon1van.commons.ids;

import static io.github.jon1van.commons.ids.BitAndHashingUtils.compute64BitHash;
import static io.github.jon1van.commons.ids.BitAndHashingUtils.makeBitMask;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class SmallTimeIdTest {

    @Test
    public void timeEpochMillsAre41bits() {

        long epochMills = now().toEpochMilli();
        long mask = makeBitMask(41);

        assertThat(epochMills).isEqualTo(mask & epochMills);
    }

    @Test
    public void timeEpochMillsAre42bits_in20Years() {

        long epochMillsInFuture = now().plus(20 * 365, ChronoUnit.DAYS).toEpochMilli();

        long mask_41 = makeBitMask(41);
        long mask_42 = makeBitMask(42);

        // these 2 assertions use bitwise AND

        // 41 bits CANNOT store an epochMillis for the semi-distant future
        assertThat(epochMillsInFuture).isNotEqualTo(mask_41 & epochMillsInFuture);

        // 42 bits CAN store an epochMillis for the semi-distant future
        assertThat(epochMillsInFuture).isEqualTo(mask_42 & epochMillsInFuture);
    }

    @Test
    public void basicConstructorUse() {

        long someNonTimeBits = Long.parseLong("1010101011101", 2);
        Instant baseTime = Instant.now();

        SmallTimeId timeId = new SmallTimeId(baseTime, someNonTimeBits);

        assertThat(timeId.nonTimeBits()).isEqualTo(someNonTimeBits);
        assertThat(timeId.timeAsEpochMs()).isEqualTo(baseTime.toEpochMilli());

        // the "base timestamp" is encoded in bits [63-22]
        // the "non time bits" are encoded in bits [21-1]
        // Therefore, a bit shift operation and a bitwise OR should generate our id
        long expectedID = (timeId.timeAsEpochMs() << 21) | someNonTimeBits;

        assertThat(timeId.id()).isEqualTo(expectedID);
    }

    @Test
    public void bulkConstructionWithOrdering() {
        /* Verify a bunch of TimeIds encoding the SAME epoch time have a stable ordering based on low-order bits. */

        Instant baseTime = Instant.now();
        int N = 100;

        // Build this list and immediately sort it...
        List<SmallTimeId> ids = IntStream.range(0, N)
                .mapToObj(i -> new SmallTimeId(baseTime, compute64BitHash(Integer.toString(i))))
                .sorted()
                .collect(Collectors.toList());

        // this time we don't sort the list at build time...
        List<SmallTimeId> ids_round2 = IntStream.range(0, N)
                .mapToObj(i -> new SmallTimeId(baseTime, compute64BitHash(Integer.toString(i))))
                .collect(Collectors.toList());

        // Instead we will throw in an extra shuffle just for fun and then sort...
        Collections.shuffle(ids_round2);
        Collections.sort(ids_round2);

        // Since both Lists encode the same source data their post-sort order should be identical
        for (int i = 0; i < N; i++) {
            assertThat(ids.get(i)).isEqualTo(ids_round2.get(i));
            assertThat(ids.get(i).id()).isEqualTo(ids_round2.get(i).id());
            assertThat(ids.get(i).time()).isEqualTo(ids_round2.get(i).time());
            assertThat(ids.get(i).nonTimeBits()).isEqualTo(ids_round2.get(i).nonTimeBits());
        }
    }

    @Test
    public void hexEncodingAndDecoding() {

        long someNonTimeBits = Long.parseLong("1010101011101", 2);

        SmallTimeId timeId = new SmallTimeId(Instant.now(), someNonTimeBits);
        SmallTimeId fromHex = SmallTimeId.fromString(timeId.toString());

        assertThat(timeId).isEqualTo(fromHex);
    }
}

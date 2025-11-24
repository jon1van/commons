package io.github.jon1van.commons.ids;

import static io.github.jon1van.commons.ids.BitAndHashingUtils.compute64BitHash;
import static io.github.jon1van.commons.ids.BitAndHashingUtils.truncateBits;
import static io.github.jon1van.commons.ids.TimeIds.directBitsetTimeId;
import static java.lang.Long.toBinaryString;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class SmallTimeIdsTest {

    @Test
    public void verifyDirectBitsetTimeId() {

        long manyRandomBits_1 = compute64BitHash("hello");
        long manyRandomBits_2 = compute64BitHash("goodbye");

        assertThat(toBinaryString(manyRandomBits_1))
                .isEqualTo("1011010010001011111001011010100100110001001110000000110011101000");
        assertThat(toBinaryString(manyRandomBits_2))
                .isEqualTo("1000010111100111110001101110100010011101011101001111111011001111");

        Instant baseTime = Instant.EPOCH;
        Instant nextTime = baseTime.plusMillis(100);

        SmallTimeId id_1 = directBitsetTimeId(baseTime, manyRandomBits_1);
        SmallTimeId id_2 = directBitsetTimeId(baseTime, manyRandomBits_2);
        SmallTimeId id_3 = directBitsetTimeId(nextTime, manyRandomBits_1);
        SmallTimeId id_4 = directBitsetTimeId(nextTime, manyRandomBits_2);

        assertThat(id_1.time()).isEqualTo(baseTime);
        assertThat(id_2.time()).isEqualTo(baseTime);
        assertThat(id_3.time()).isEqualTo(nextTime);
        assertThat(id_4.time()).isEqualTo(nextTime);

        // aka the last 21 bits of the "manyRandomBits_N" values...
        assertThat(id_1.nonTimeBits()).isEqualTo(truncateBits(manyRandomBits_1, 21));
        assertThat(id_2.nonTimeBits()).isEqualTo(truncateBits(manyRandomBits_2, 21));
        assertThat(id_3.nonTimeBits()).isEqualTo(truncateBits(manyRandomBits_1, 21));
        assertThat(id_4.nonTimeBits()).isEqualTo(truncateBits(manyRandomBits_2, 21));

        // aka the last 21 bits of the "manyRandomBits_N" values (but this test is for human readability)
        assertThat(toBinaryString(id_1.nonTimeBits())).isEqualTo("110000000110011101000");
        assertThat(toBinaryString(id_2.nonTimeBits())).isEqualTo("101001111111011001111");
        assertThat(toBinaryString(id_3.nonTimeBits())).isEqualTo("110000000110011101000");
        assertThat(toBinaryString(id_4.nonTimeBits())).isEqualTo("101001111111011001111");
    }
}

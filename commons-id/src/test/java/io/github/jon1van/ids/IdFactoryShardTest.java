package io.github.jon1van.ids;

import static io.github.jon1van.ids.IdFactoryShard.*;
import static java.lang.Long.toBinaryString;
import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class IdFactoryShardTest {

    @Test
    public void testNumBitsRequiredFor() {

        assertThrows(IllegalArgumentException.class, () -> numBitsRequiredFor(0));

        assertAll(
                () -> assertThat(numBitsRequiredFor(1)).isEqualTo(0),
                () -> assertThat(numBitsRequiredFor(2)).isEqualTo(1),
                () -> assertThat(numBitsRequiredFor(3)).isEqualTo(2),
                () -> assertThat(numBitsRequiredFor(4)).isEqualTo(2),
                () -> assertThat(numBitsRequiredFor(8)).isEqualTo(3),
                () -> assertThat(numBitsRequiredFor(16)).isEqualTo(4),
                () -> assertThat(numBitsRequiredFor(32)).isEqualTo(5));
    }

    @Test
    public void testBitCountsAndItemLimit_1shard() {
        IdFactoryShard factory = new IdFactoryShard(0, 1);

        assertThat(factory.shardIndex()).isEqualTo(0);
        assertThat(factory.numBitsToStoreShardIndex()).isEqualTo(0);
        assertThat(factory.numBitsForItemDistinction()).isEqualTo(21);
        assertThat(factory.limitTimeIdsPerEpochMills()).isEqualTo(2_097_152);
    }

    @Test
    public void testBitCountsAndItemLimit_2shards() {
        IdFactoryShard factory = new IdFactoryShard(0, 2);

        assertThat(factory.shardIndex()).isEqualTo(0);
        assertThat(factory.numBitsToStoreShardIndex()).isEqualTo(1);
        assertThat(factory.numBitsForItemDistinction()).isEqualTo(20);
        assertThat(factory.limitTimeIdsPerEpochMills()).isEqualTo(1_048_576);
    }

    @Test
    public void testBitCountsAndItemLimit_3shards() {
        IdFactoryShard factory = new IdFactoryShard(2, 3);

        assertThat(factory.shardIndex()).isEqualTo(2);
        assertThat(factory.numBitsToStoreShardIndex()).isEqualTo(2);
        assertThat(factory.numBitsForItemDistinction()).isEqualTo(19);
        assertThat(factory.limitTimeIdsPerEpochMills()).isEqualTo(524_288);
    }

    @Test
    public void testBitCountsAndItemLimit_4shards() {
        IdFactoryShard factory = new IdFactoryShard(0, 4);

        assertThat(factory.shardIndex()).isEqualTo(0);
        assertThat(factory.numBitsToStoreShardIndex()).isEqualTo(2);
        assertThat(factory.numBitsForItemDistinction()).isEqualTo(19);
        assertThat(factory.limitTimeIdsPerEpochMills()).isEqualTo(524_288);
    }

    @Test
    public void limitOnShardIndexIsEnforced() {
        // if you only have 1 shard the max "shardIndex" is zero
        assertDoesNotThrow(() -> new IdFactoryShard(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new IdFactoryShard(1, 1));

        // if you only have 5 shard the max "shardIndex" is 4
        assertDoesNotThrow(() -> new IdFactoryShard(4, 5));
        assertThrows(IllegalArgumentException.class, () -> new IdFactoryShard(5, 5));
    }

    @Test
    public void testLimitPerInstant_1MillionShards_shardIndex0() {
        IdFactoryShard factory = new IdFactoryShard(0, 1_048_576);

        SmallTimeId id0 = factory.generateIdFor(EPOCH);
        SmallTimeId id1 = factory.generateIdFor(EPOCH);

        assertThat(toBinaryString(id0.nonTimeBits())).isEqualTo("0"); // 21 bits of 00000...0
        assertThat(toBinaryString(id1.nonTimeBits())).isEqualTo("1"); // 21 bits of 00000...1
        assertThat(id0.nonTimeBits()).isEqualTo(0L);
        assertThat(id1.nonTimeBits()).isEqualTo(1L);

        // trying to get a 3rd TimeId while referencing the same EpochMill will fail because you only had 1 bit to work
        // with
        assertThrows(NoSuchElementException.class, () -> factory.generateIdFor(EPOCH));
    }

    @Test
    public void testLimitPerInstant_1MillionShards_shardIndex1_048_575() {
        IdFactoryShard factory = new IdFactoryShard(1_048_575, 1_048_576);

        assertThat(factory.numBitsForItemDistinction()).isEqualTo(1);

        SmallTimeId id0 = factory.generateIdFor(EPOCH); // power of 2 - 2
        SmallTimeId id1 = factory.generateIdFor(EPOCH); // power of 2 - 1

        assertThat(toBinaryString(id0.nonTimeBits())).isEqualTo("111111111111111111110");
        assertThat(toBinaryString(id1.nonTimeBits())).isEqualTo("111111111111111111111");

        assertThat(id0.nonTimeBits()).isEqualTo((long) (2_097_152 - 2));
        assertThat(id1.nonTimeBits()).isEqualTo((long) (2_097_152 - 1));

        // trying to get a 3rd TimeId while referencing the same EpochMill will fail because you only had 1 bit to work
        // with
        assertThrows(NoSuchElementException.class, () -> factory.generateIdFor(EPOCH));
    }

    @Test
    public void testLimitPerInstant_500kShards() {
        // 524_288 = 2^19
        IdFactoryShard factory = new IdFactoryShard(0, 524_288);

        assertThat(factory.numBitsForItemDistinction()).isEqualTo(2);

        SmallTimeId id0 = factory.generateIdFor(EPOCH);
        SmallTimeId id1 = factory.generateIdFor(EPOCH);
        SmallTimeId id2 = factory.generateIdFor(EPOCH);
        SmallTimeId id3 = factory.generateIdFor(EPOCH);

        assertThat(toBinaryString(id0.nonTimeBits())).isEqualTo("0");
        assertThat(toBinaryString(id1.nonTimeBits())).isEqualTo("1");
        assertThat(toBinaryString(id2.nonTimeBits())).isEqualTo("10");
        assertThat(toBinaryString(id3.nonTimeBits())).isEqualTo("11");
        assertThat(id0.nonTimeBits()).isEqualTo(0L);
        assertThat(id1.nonTimeBits()).isEqualTo(1L);
        assertThat(id2.nonTimeBits()).isEqualTo(2L);
        assertThat(id3.nonTimeBits()).isEqualTo(3L);

        // trying to get a 5th TimeId while referencing the same EpochMill will fail because you only had 2 bit to work
        // with
        assertThrows(NoSuchElementException.class, () -> factory.generateIdFor(EPOCH));
    }

    @Test
    public void multipleFactoriesCombineToCoverTheBitSpace() {
        // Together, these for factories should create 2_097_152 unique TimeIds that map to EPOCH
        IdFactoryShard factory0 = new IdFactoryShard(0, 4);
        IdFactoryShard factory1 = new IdFactoryShard(1, 4);
        IdFactoryShard factory2 = new IdFactoryShard(2, 4);
        IdFactoryShard factory3 = new IdFactoryShard(3, 4);

        TreeSet<SmallTimeId> idsThemselves = new TreeSet<>();
        TreeSet<Long> idBitsets = new TreeSet<>();
        Instant sharedTimeStamp = EPOCH;

        for (int i = 0; i < 524_288; i++) {
            // Every factory makes a TimeId
            SmallTimeId id0 = factory0.generateIdFor(sharedTimeStamp);
            SmallTimeId id1 = factory1.generateIdFor(sharedTimeStamp);
            SmallTimeId id2 = factory2.generateIdFor(sharedTimeStamp);
            SmallTimeId id3 = factory3.generateIdFor(sharedTimeStamp);

            // Save the TimeId
            idsThemselves.add(id0);
            idsThemselves.add(id1);
            idsThemselves.add(id2);
            idsThemselves.add(id3);

            // Save the full bitsets
            idBitsets.add(id0.id());
            idBitsets.add(id1.id());
            idBitsets.add(id2.id());
            idBitsets.add(id3.id());
        }

        // All TimeIds and Bitset Longs were unique! ....
        assertThat(idsThemselves.size()).isEqualTo(2_097_152);
        assertThat(idBitsets.size()).isEqualTo(2_097_152);

        // all 4 factories will fail if they ask for one more TimeId using the "sharedTimeStamp" that was just fully
        // allocated
        assertThrows(NoSuchElementException.class, () -> factory0.generateIdFor(sharedTimeStamp));
        assertThrows(NoSuchElementException.class, () -> factory1.generateIdFor(sharedTimeStamp));
        assertThrows(NoSuchElementException.class, () -> factory2.generateIdFor(sharedTimeStamp));
        assertThrows(NoSuchElementException.class, () -> factory3.generateIdFor(sharedTimeStamp));

        // BUT all 4 factories have no problem getting a TimeId that hasn't been allocated
        assertDoesNotThrow(() -> factory0.generateIdFor(sharedTimeStamp.plusMillis(1)));
        assertDoesNotThrow(() -> factory1.generateIdFor(sharedTimeStamp.plusMillis(1)));
        assertDoesNotThrow(() -> factory2.generateIdFor(sharedTimeStamp.plusMillis(1)));
        assertDoesNotThrow(() -> factory3.generateIdFor(sharedTimeStamp.plusMillis(1)));
    }

    @Test
    public void inMemoryCounterWorks() {

        CountKeeper counter = inMemoryCounter();

        assertThat(counter.nextCountFor(EPOCH)).isEqualTo(0);
        assertThat(counter.nextCountFor(EPOCH)).isEqualTo(1);

        assertThat(counter.nextCountFor(EPOCH.plusMillis(1))).isEqualTo(0);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(1))).isEqualTo(1);
    }

    @Test
    public void limitedMemoryCounterWorks_happyPath() {

        IdFactoryShard.CountKeeper counter = limitedMemoryCounter(4);

        assertThat(counter.nextCountFor(EPOCH)).isEqualTo(0);
        assertThat(counter.nextCountFor(EPOCH)).isEqualTo(1);

        assertThat(counter.nextCountFor(EPOCH.plusMillis(1))).isEqualTo(0);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(1))).isEqualTo(1);
    }

    @Test
    public void limitedMemoryCounterWorks_evictionPath() {

        IdFactoryShard.CountKeeper counter = limitedMemoryCounter(3);

        assertThat(counter.nextCountFor(EPOCH.plusMillis(0))).isEqualTo(0);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(1))).isEqualTo(0);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(2))).isEqualTo(0);

        assertThat(counter.nextCountFor(EPOCH.plusMillis(0))).isEqualTo(1);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(1))).isEqualTo(1);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(2))).isEqualTo(1);

        assertThat(counter.nextCountFor(EPOCH.plusMillis(0))).isEqualTo(2);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(1))).isEqualTo(2);
        assertThat(counter.nextCountFor(EPOCH.plusMillis(2))).isEqualTo(2);

        // trigger the eviction of the oldest "time counter" by requesting a counter for a 4th unique timestamp..
        assertThat(counter.nextCountFor(EPOCH.plusSeconds(1))).isEqualTo(0);

        assertThrows(IllegalStateException.class, () -> counter.nextCountFor(EPOCH));
    }

    enum SimpleEnum {
        CASE_A,
        CASE_B,
        CASE_C,
        CASE_D
    }

    @Test
    public void enumStaticFactoryWorksWithEnum() {

        IdFactoryShard factory_A = IdFactoryShard.newFactoryShardFor(SimpleEnum.CASE_A);
        IdFactoryShard factory_B = IdFactoryShard.newFactoryShardFor(SimpleEnum.CASE_B);
        IdFactoryShard factory_C = IdFactoryShard.newFactoryShardFor(SimpleEnum.CASE_C);
        IdFactoryShard factory_D = IdFactoryShard.newFactoryShardFor(SimpleEnum.CASE_D);

        assertThat(factory_A.shardIndex()).isEqualTo(0);
        assertThat(factory_A.numShardsInTeam()).isEqualTo(4);
        assertThat(factory_A.numBitsToStoreShardIndex()).isEqualTo(2);

        assertThat(factory_B.shardIndex()).isEqualTo(1);
        assertThat(factory_B.numShardsInTeam()).isEqualTo(4);
        assertThat(factory_B.numBitsToStoreShardIndex()).isEqualTo(2);

        assertThat(factory_C.shardIndex()).isEqualTo(2);
        assertThat(factory_C.numShardsInTeam()).isEqualTo(4);
        assertThat(factory_C.numBitsToStoreShardIndex()).isEqualTo(2);

        assertThat(factory_D.shardIndex()).isEqualTo(3);
        assertThat(factory_D.numShardsInTeam()).isEqualTo(4);
        assertThat(factory_D.numBitsToStoreShardIndex()).isEqualTo(2);
    }
}

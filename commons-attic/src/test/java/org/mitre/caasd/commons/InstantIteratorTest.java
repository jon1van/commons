package org.mitre.caasd.commons;

import static java.time.Instant.EPOCH;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class InstantIteratorTest {

    @Test
    public void testConstruction() {
        InstantIterator instance = new InstantIterator(EPOCH, EPOCH.plusSeconds(10), Duration.ofSeconds(5));

        assertEquals(instance.hasNext(), true);
        assertEquals(instance.next(), EPOCH);
        assertEquals(instance.next(), EPOCH.plusSeconds(5));
        assertEquals(instance.next(), EPOCH.plusSeconds(10));
        assertFalse(instance.hasNext());
    }

    @Test
    public void test_tooManyCallsToNextFails() {
        InstantIterator instance = new InstantIterator(EPOCH, EPOCH.plusSeconds(10), Duration.ofSeconds(5));
        instance.next(); // epoch
        instance.next(); // epoch + 5sec
        instance.next(); // epoch + 10sec

        assertThrows(NoSuchElementException.class, () -> instance.next()); // this call fails
    }

    @Test
    public void testEndOfWindowIsIncluded() {
        InstantIterator instance = new InstantIterator(EPOCH, EPOCH.plusSeconds(11), Duration.ofSeconds(5));

        assertEquals(instance.hasNext(), true);
        assertEquals(instance.next(), EPOCH);
        assertEquals(instance.next(), EPOCH.plusSeconds(5));
        assertEquals(instance.next(), EPOCH.plusSeconds(10));
        assertEquals(instance.next(), EPOCH.plusSeconds(11));
        assertEquals(instance.hasNext(), false);
    }

    @Test
    public void testConstruction_zeroTimeStep() {
        // fails when a zero timeStep is supplied
        assertThrows(
                IllegalArgumentException.class,
                () -> new InstantIterator(EPOCH, EPOCH.plusSeconds(300), Duration.ofSeconds(0)));
    }

    @Test
    public void testConstruction_negativeTimeStep() {
        // fails when a negative timeStep is supplied
        assertThrows(
                IllegalArgumentException.class,
                () -> new InstantIterator(EPOCH, EPOCH.plusSeconds(300), Duration.ofSeconds(-5)));
    }
}

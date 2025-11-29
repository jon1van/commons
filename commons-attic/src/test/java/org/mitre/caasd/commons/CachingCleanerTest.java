package org.mitre.caasd.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CachingCleanerTest {

    @Test
    public void testConstructorWiring() {

        DataCleaner<String> stringCleaner = (String s) -> Optional.of(s.toLowerCase());

        CachingCleaner<String> instance = new CachingCleaner<>(stringCleaner, 5);

        assertEquals(stringCleaner, instance.cleaner());
    }

    @Test
    public void testCleaningBehavior() {

        DataCleaner<String> cleaner = (String s) -> Optional.of(s.toLowerCase());

        CachingCleaner<String> instance = new CachingCleaner<>(cleaner, 5);

        Optional<String> result = instance.clean("ISLOWERCASE");
        assertTrue(result.isPresent());
        assertEquals("islowercase", result.get());
    }

    @Test
    public void testCacheBehavior() {

        DataCleaner<String> cleaner = (String s) -> Optional.of(s.toLowerCase());

        CachingCleaner<String> instance = new CachingCleaner<>(cleaner, 1);

        Optional<String> result = instance.clean("ISLOWERCASE");

        // empty cache -- expect 0 hits and 0 evictions
        assertEquals("islowercase", result.get());
        assertEquals(instance.cache().stats().hitCount(), 0);
        assertEquals(instance.cache().stats().evictionCount(), 0);

        // reuse the same key, expect 1 hit and 0 evictions
        instance.clean("ISLOWERCASE");
        assertEquals(instance.cache().stats().hitCount(), 1);
        assertEquals(instance.cache().stats().evictionCount(), 0);

        // new key, the old value in the cache should get evicted
        Optional<String> newResult = instance.clean("NEXT");
        assertEquals("next", newResult.get());
        assertEquals(instance.cache().stats().hitCount(), 1);
        assertEquals(instance.cache().stats().evictionCount(), 1);

        // reuse the same key, expect another hit
        instance.clean("NEXT");
        assertEquals(instance.cache().stats().hitCount(), 2);
        assertEquals(instance.cache().stats().evictionCount(), 1);
    }
}

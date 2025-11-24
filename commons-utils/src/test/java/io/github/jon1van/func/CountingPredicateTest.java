package io.github.jon1van.func;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class CountingPredicateTest {

    static class GreaterThanTen implements Predicate<Integer> {
        @Override
        public boolean test(Integer i) {
            return i > 10;
        }
    }

    @Test
    void testBasicUsage() {

        Predicate<Integer> greaterThanTen = new GreaterThanTen();
        CountingPredicate<Integer> counter = CountingPredicate.from(greaterThanTen);

        counter.test(11);
        counter.test(12);
        counter.test(13);

        assertThat(counter.trueCount()).isEqualTo(3L);
        assertThat(counter.falseCount()).isEqualTo(0L);
        assertThat(counter.count()).isEqualTo(3L);

        counter.test(8);

        assertThat(counter.trueCount()).isEqualTo(3L);
        assertThat(counter.falseCount()).isEqualTo(1L);
        assertThat(counter.count()).isEqualTo(4L);

        assertThat(counter.predicate()).isEqualTo(greaterThanTen);
    }
}

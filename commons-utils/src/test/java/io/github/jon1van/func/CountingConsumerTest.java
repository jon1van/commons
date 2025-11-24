package io.github.jon1van.func;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class CountingConsumerTest {

    static class IntegerGarbageCan implements Consumer<Integer> {
        @Override
        public void accept(Integer i) {}
    }

    @Test
    void testBasicUsage() {

        Consumer<Integer> garbageCan = new IntegerGarbageCan();
        CountingConsumer<Integer> counter = CountingConsumer.from(garbageCan);

        counter.accept(11);
        counter.accept(12);
        counter.accept(13);
        assertThat(counter.acceptCount()).isEqualTo(3L);
        counter.accept(8);
        assertThat(counter.acceptCount()).isEqualTo(4L);

        assertThat(counter.consumer()).isEqualTo(garbageCan);
    }
}

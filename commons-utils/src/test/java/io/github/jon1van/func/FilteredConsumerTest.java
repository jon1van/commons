package io.github.jon1van.func;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class FilteredConsumerTest {

    @Test
    public void inputsAreSubmittedToFilter() {

        CountingPredicate<String> filter = new CountingPredicate<>(x -> true);
        FilteredConsumer<String> filterConsumer = new FilteredConsumer<>(filter, x -> {});
        assertThat(filter.count()).isEqualTo(0);
        filterConsumer.accept("hello");
        assertThat(filter.count()).isEqualTo(1);
    }

    @Test
    public void testFailingFilterDoesntForwardToConsumer() {

        CountingConsumer<String> counter = new CountingConsumer<>(x -> {});

        FilteredConsumer<String> filteredConsumer = new FilteredConsumer<>(alwaysFalse(), counter);

        assertThat(counter.acceptCount()).isEqualTo(0);
        filteredConsumer.accept("testString");
        assertThat(counter.acceptCount()).isEqualTo(0);
    }

    @Test
    public void testPassingFilterForwardsToConsumer() {

        CountingConsumer<String> testConsumer = new CountingConsumer<>(x -> {});

        FilteredConsumer<String> filteredConsumer = new FilteredConsumer<>(alwaysTrue(), testConsumer);

        assertThat(testConsumer.acceptCount()).isEqualTo(0);
        filteredConsumer.accept("testString");
        assertThat(testConsumer.acceptCount()).isEqualTo(1);
    }

    @Test
    public void whenTrueConsumerReceivesWhenTrue() {

        FilteredConsumer<Integer> fc = new FilteredConsumer<>(x -> true, new CountingConsumer<>(x -> {}), x -> {});

        fc.accept(12);

        assertThat(((CountingConsumer<Integer>) fc.whenTrue()).acceptCount()).isEqualTo(1);
    }

    @Test
    public void whenFalseConsumerReceivesWhenFalse() {

        FilteredConsumer<Integer> fc = new FilteredConsumer<>(x -> false, x -> {}, new CountingConsumer<>(x -> {}));

        fc.accept(12);

        assertThat(((CountingConsumer<Integer>) fc.whenFalse()).acceptCount()).isEqualTo(1);
    }

    @Test
    public void predicateIsRequired() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new FilteredConsumer<>(null, x -> {}, x -> {}));
    }

    @Test
    public void whenTrueConsumerIsRequired() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new FilteredConsumer<>(alwaysTrue(), null, x -> {}));
    }

    @Test
    public void whenFalseConsumerIsRequired() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new FilteredConsumer<>(alwaysTrue(), x -> {}, null));
    }
}

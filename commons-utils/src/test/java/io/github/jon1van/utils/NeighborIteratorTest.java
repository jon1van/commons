package io.github.jon1van.utils;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class NeighborIteratorTest {

    @Test
    public void hasNextWorks() {
        List<Integer> numbers = newArrayList(1, 2, 3);
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        assertThat(neighborIterator.hasNext()).isTrue();
        neighborIterator.next(); // 1 & 2
        assertThat(neighborIterator.hasNext()).isTrue();
        neighborIterator.next(); // 2 & 3
        assertThat(neighborIterator.hasNext()).isFalse();
    }

    @Test
    public void hasNextWorks_emptyList() {
        List<Integer> numbers = newArrayList();
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        assertThat(neighborIterator.hasNext()).isFalse();
    }

    @Test
    public void isEmptyWorks_emptyList() {
        List<Integer> numbers = newArrayList();
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        assertThat(neighborIterator.wasEmpty()).isTrue();
    }

    @Test
    public void isEmptyWorks_listWithContent() {
        List<Integer> numbers = newArrayList(1, 2, 3);
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        assertThat(neighborIterator.wasEmpty()).isFalse();
    }

    @Test
    public void hasNextWorks_nullEntry() {
        List<Integer> numbers = newArrayList(1, null, 3);
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        assertThat(neighborIterator.hasNext()).isTrue();
        neighborIterator.next(); // 1 & null
        assertThat(neighborIterator.hasNext()).isTrue();
        neighborIterator.next(); // null & 3
        assertThat(neighborIterator.hasNext()).isFalse();
    }

    @Test
    public void nextGivesExpectedElement() {
        List<Integer> numbers = newArrayList(1, 2, 3);
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        IterPair<Integer> first = neighborIterator.next();
        assertThat(first.prior()).isEqualTo(1);
        assertThat(first.current()).isEqualTo(2);

        IterPair<Integer> second = neighborIterator.next();
        assertThat(second.prior()).isEqualTo(2);
        assertThat(second.current()).isEqualTo(3);

        assertThat(neighborIterator.hasNext()).isFalse();
    }

    @Test
    public void nextGivesExpectedElement_nullEntry() {
        List<Integer> numbers = newArrayList(1, null, 3);
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        IterPair<Integer> first = neighborIterator.next();
        assertThat(first.prior()).isEqualTo(1);
        assertThat(first.current()).isNull();

        IterPair<Integer> second = neighborIterator.next();
        assertThat(second.prior()).isNull();
        assertThat(second.current()).isEqualTo(3);

        assertThat(neighborIterator.hasNext()).isFalse();
    }

    @Test
    public void noSuchElementExceptionIfNoNextElement() {
        List<Integer> numbers = newArrayList();
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        assertThrows(NoSuchElementException.class, () -> neighborIterator.next());
    }

    @Test
    public void canGetTheLoanElement() {
        List<Integer> numbers = newArrayList(1);
        NeighborIterator<Integer> neighborIterator = new NeighborIterator<>(numbers.iterator());

        assertThat(neighborIterator.hasNext()).isFalse();
        assertThat(neighborIterator.hadExactlyOneElement()).isTrue();
        assertThat(neighborIterator.getSoleElement()).isEqualTo(1);
    }
}

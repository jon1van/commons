package io.github.jon1van.units;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.units.HasTime.nearest;
import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import io.github.jon1van.units.TimeTest.HasTimeImp;
import org.junit.jupiter.api.Test;

public class HasTimeTest {

    @Test
    public void validateRejectsNull() {
        assertThrows(IllegalStateException.class, () -> HasTime.validate(null));
    }

    @Test
    public void validateRejectsInstantsAtEpoch() {
        assertThrows(IllegalStateException.class, () -> HasTime.validate(EPOCH));
    }

    @Test
    public void validateRejectsInstantsBeforeEpoch() {
        assertThrows(IllegalStateException.class, () -> HasTime.validate(EPOCH.minusMillis(1)));
    }

    @Test
    public void validateAcceptsInstantsAfterEpoch() {
        HasTime.validate(EPOCH.plusMillis(1)); // passes validation, no other side effect
    }

    @Test
    public void durationBtw_correct_alwaysPositiveNoMatterOrder() {
        HasTimeImp a = new HasTimeImp(EPOCH);
        HasTimeImp b = new HasTimeImp(EPOCH.plusSeconds(10));

        assertThat(a.durationBtw(b)).isEqualTo(Duration.ofSeconds(10));
        assertThat(b.durationBtw(a)).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    public void testNearest() {
        HasTimeImp a = new HasTimeImp(EPOCH);
        HasTimeImp b = new HasTimeImp(EPOCH.plusSeconds(10));

        assertThat(nearest(a, b, EPOCH.plusSeconds(1))).isEqualTo(a);
        assertThat(nearest(a, b, EPOCH.plusSeconds(9))).isEqualTo(b);
        assertThat(nearest(a, b, EPOCH.plusSeconds(5))).isEqualTo(a);
    }

    @Test
    public void binarySearchMatchesGiveCorrectIndex() {

        ArrayList<PojoWithTime> list = newArrayList(
                new PojoWithTime("a", EPOCH),
                new PojoWithTime("b", EPOCH.plusSeconds(10)),
                new PojoWithTime("c", EPOCH.plusSeconds(20)));

        assertThat(HasTime.binarySearch(list, EPOCH)).isEqualTo(0);
        assertThat(HasTime.binarySearch(list, EPOCH.plusSeconds(10))).isEqualTo(1);
        assertThat(HasTime.binarySearch(list, EPOCH.plusSeconds(20))).isEqualTo(2);
    }

    @Test
    public void binarySearchMissesGiveCorrectIndex() {

        ArrayList<PojoWithTime> list = newArrayList(
                new PojoWithTime("a", EPOCH),
                new PojoWithTime("b", EPOCH.plusSeconds(10)),
                new PojoWithTime("c", EPOCH.plusSeconds(20)));

        // output int = (-(insertion point) -1)
        assertThat(HasTime.binarySearch(list, EPOCH.minusSeconds(1))).isEqualTo(-1); // insert before item 0
        assertThat(HasTime.binarySearch(list, EPOCH.plusSeconds(1))).isEqualTo(-2); // insert between 0 and 1
        assertThat(HasTime.binarySearch(list, EPOCH.plusSeconds(11))).isEqualTo(-3); // insert between 1 and 2
        assertThat(HasTime.binarySearch(list, EPOCH.plusSeconds(21))).isEqualTo(-4); // insert after 2
    }

    @Test
    public void floorGivesCorrectItem() {
        PojoWithTime a = new PojoWithTime("a", EPOCH);
        PojoWithTime b = new PojoWithTime("b", EPOCH.plusSeconds(10));
        PojoWithTime c = new PojoWithTime("c", EPOCH.plusSeconds(20));
        PojoWithTime d = new PojoWithTime("d", EPOCH.plusSeconds(30));

        ArrayList<PojoWithTime> list = newArrayList(a, b, c, d);

        assertThat(HasTime.floor(list, EPOCH)).isEqualTo(a);
        assertThat(HasTime.floor(list, EPOCH.plusSeconds(10))).isEqualTo(b);
        assertThat(HasTime.floor(list, EPOCH.plusSeconds(20))).isEqualTo(c);
        assertThat(HasTime.floor(list, EPOCH.plusSeconds(30))).isEqualTo(d);

        assertThat(HasTime.floor(list, EPOCH.plusSeconds(5))).isEqualTo(a);
        assertThat(HasTime.floor(list, EPOCH.plusSeconds(15))).isEqualTo(b);
        assertThat(HasTime.floor(list, EPOCH.plusSeconds(25))).isEqualTo(c);
    }

    @Test
    void floorRejectsOutOfRange() {

        PojoWithTime a = new PojoWithTime("a", EPOCH);
        PojoWithTime b = new PojoWithTime("b", EPOCH.plusSeconds(10));
        PojoWithTime c = new PojoWithTime("c", EPOCH.plusSeconds(20));
        PojoWithTime d = new PojoWithTime("d", EPOCH.plusSeconds(30));

        ArrayList<PojoWithTime> list = newArrayList(a, b, c, d);

        assertThrows(IllegalArgumentException.class, () -> HasTime.floor(list, EPOCH.minusSeconds(1)));

        assertThrows(IllegalArgumentException.class, () -> HasTime.floor(list, EPOCH.plusSeconds(31)));
    }

    @Test
    public void ceilingGivesCorrectItem() {
        PojoWithTime a = new PojoWithTime("a", EPOCH);
        PojoWithTime b = new PojoWithTime("b", EPOCH.plusSeconds(10));
        PojoWithTime c = new PojoWithTime("c", EPOCH.plusSeconds(20));
        PojoWithTime d = new PojoWithTime("d", EPOCH.plusSeconds(30));

        ArrayList<PojoWithTime> list = newArrayList(a, b, c, d);

        assertThat(HasTime.ceiling(list, EPOCH)).isEqualTo(a);
        assertThat(HasTime.ceiling(list, EPOCH.plusSeconds(10))).isEqualTo(b);
        assertThat(HasTime.ceiling(list, EPOCH.plusSeconds(20))).isEqualTo(c);
        assertThat(HasTime.ceiling(list, EPOCH.plusSeconds(30))).isEqualTo(d);

        assertThat(HasTime.ceiling(list, EPOCH.plusSeconds(5))).isEqualTo(b);
        assertThat(HasTime.ceiling(list, EPOCH.plusSeconds(15))).isEqualTo(c);
        assertThat(HasTime.ceiling(list, EPOCH.plusSeconds(25))).isEqualTo(d);
    }

    @Test
    void ceilingRejectsOutOfRange() {

        PojoWithTime a = new PojoWithTime("a", EPOCH);
        PojoWithTime b = new PojoWithTime("b", EPOCH.plusSeconds(10));
        PojoWithTime c = new PojoWithTime("c", EPOCH.plusSeconds(20));
        PojoWithTime d = new PojoWithTime("d", EPOCH.plusSeconds(30));

        ArrayList<PojoWithTime> list = newArrayList(a, b, c, d);

        assertThrows(IllegalArgumentException.class, () -> HasTime.ceiling(list, EPOCH.minusSeconds(1)));

        assertThrows(IllegalArgumentException.class, () -> HasTime.ceiling(list, EPOCH.plusSeconds(31)));
    }

    @Test
    public void closestGivesCorrectItem() {
        PojoWithTime a = new PojoWithTime("a", EPOCH);
        PojoWithTime b = new PojoWithTime("b", EPOCH.plusSeconds(10));
        PojoWithTime c = new PojoWithTime("c", EPOCH.plusSeconds(20));
        PojoWithTime d = new PojoWithTime("d", EPOCH.plusSeconds(30));

        ArrayList<PojoWithTime> list = newArrayList(a, b, c, d);

        assertThat(HasTime.closest(list, EPOCH)).isEqualTo(a);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(10))).isEqualTo(b);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(20))).isEqualTo(c);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(30))).isEqualTo(d);

        assertThat(HasTime.closest(list, EPOCH.plusSeconds(1))).isEqualTo(a);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(5))).isEqualTo(a);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(9))).isEqualTo(b);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(11))).isEqualTo(b);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(15))).isEqualTo(b);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(19))).isEqualTo(c);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(21))).isEqualTo(c);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(25))).isEqualTo(c);
        assertThat(HasTime.closest(list, EPOCH.plusSeconds(29))).isEqualTo(d);
    }

    @Test
    void closestRejectsOutOfRange() {

        PojoWithTime a = new PojoWithTime("a", EPOCH);
        PojoWithTime b = new PojoWithTime("b", EPOCH.plusSeconds(10));
        PojoWithTime c = new PojoWithTime("c", EPOCH.plusSeconds(20));
        PojoWithTime d = new PojoWithTime("d", EPOCH.plusSeconds(30));

        ArrayList<PojoWithTime> list = newArrayList(a, b, c, d);

        assertThrows(IllegalArgumentException.class, () -> HasTime.closest(list, EPOCH.minusSeconds(1)));

        assertThrows(IllegalArgumentException.class, () -> HasTime.closest(list, EPOCH.plusSeconds(31)));
    }

    /// This class does not implement Comparable BUT we can sort instance of PojoWithTime by its time
    /// value.  If we do this we may want to binary search a collection of these PojoWithTime for a
    /// specific time value.
    public static class PojoWithTime implements HasTime {

        final String name;

        final Instant time;

        PojoWithTime(String name, Instant time) {
            this.name = name;
            this.time = time;
        }

        @Override
        public Instant time() {
            return time;
        }
    }
}

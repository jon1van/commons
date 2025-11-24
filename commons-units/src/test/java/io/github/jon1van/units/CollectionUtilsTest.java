package io.github.jon1van.units;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static io.github.jon1van.units.CollectionUtils.binarySearch;
import static io.github.jon1van.units.HasTimeTest.*;
import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class CollectionUtilsTest {

    @Test
    public void binarySearchMatchesGiveCorrectIndex_arrayList() {

        ArrayList<PojoWithTime> list = newArrayList(
                new PojoWithTime("a", EPOCH),
                new PojoWithTime("b", EPOCH.plusSeconds(10)),
                new PojoWithTime("c", EPOCH.plusSeconds(20)));

        Function<PojoWithTime, Instant> timeGetter = PojoWithTime::time;

        assertThat(binarySearch(list, timeGetter, EPOCH)).isEqualTo(0);
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(10))).isEqualTo(1);
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(20))).isEqualTo(2);
    }

    @Test
    public void binarySearchMissesGiveCorrectIndex_arrayList() {

        ArrayList<PojoWithTime> list = newArrayList(
                new PojoWithTime("a", EPOCH),
                new PojoWithTime("b", EPOCH.plusSeconds(10)),
                new PojoWithTime("c", EPOCH.plusSeconds(20)));

        Function<PojoWithTime, Instant> timeGetter = PojoWithTime::time;

        // output int = (-(insertion point) -1)
        assertThat(binarySearch(list, timeGetter, EPOCH.minusSeconds(1))).isEqualTo(-1); // insert before item 0
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(1))).isEqualTo(-2); // insert between 0 and 1
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(11))).isEqualTo(-3); // insert between 1 and 2
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(21))).isEqualTo(-4); // insert after 2
    }

    @Test
    public void binarySearchMatchesGiveCorrectIndex_linkedList() {

        LinkedList<PojoWithTime> list = newLinkedList();
        list.add(new PojoWithTime("a", EPOCH));
        list.add(new PojoWithTime("b", EPOCH.plusSeconds(10)));
        list.add(new PojoWithTime("c", EPOCH.plusSeconds(20)));

        Function<PojoWithTime, Instant> timeGetter = PojoWithTime::time;

        assertThat(binarySearch(list, timeGetter, EPOCH)).isEqualTo(0);
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(10))).isEqualTo(1);
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(20))).isEqualTo(2);
    }

    @Test
    public void binarySearchMissesGiveCorrectIndex_linkedList() {

        LinkedList<PojoWithTime> list = newLinkedList();
        list.add(new PojoWithTime("a", EPOCH));
        list.add(new PojoWithTime("b", EPOCH.plusSeconds(10)));
        list.add(new PojoWithTime("c", EPOCH.plusSeconds(20)));

        Function<PojoWithTime, Instant> timeGetter = PojoWithTime::time;

        // output int = (-(insertion point) -1)
        assertThat(binarySearch(list, timeGetter, EPOCH.minusSeconds(1))).isEqualTo(-1); // insert before item 0
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(1))).isEqualTo(-2); // insert between 0 and 1
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(11))).isEqualTo(-3); // insert between 1 and 2
        assertThat(binarySearch(list, timeGetter, EPOCH.plusSeconds(21))).isEqualTo(-4); // insert after 2
    }
}

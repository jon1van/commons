package io.github.jon1van.units;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class TimeWindowTest {

    @Test
    public void testConstructor() {

        TimeWindow sample = new TimeWindow(EPOCH, EPOCH.plusSeconds(1));

        assertThat(EPOCH).isEqualTo(sample.start());
        assertThat(EPOCH.plusSeconds(1)).isEqualTo(sample.end());
    }

    @Test
    public void testSingleInstantWindow() {
        // this should be possible
        TimeWindow sample = new TimeWindow(EPOCH, EPOCH);

        assertTrue(sample.contains(EPOCH));
        assertTrue(sample.duration().equals(Duration.ZERO));
    }

    @Test
    public void testConstructorWithBadInput_1() {

        // Should fail because the inputs are out of order
        assertThrows(IllegalArgumentException.class, () -> TimeWindow.of(EPOCH.plusSeconds(1), EPOCH));
    }

    @Test
    public void testConstructorWithBadInput_2() {
        assertThrows(NullPointerException.class, () -> TimeWindow.of(null, EPOCH));
        assertThrows(NullPointerException.class, () -> TimeWindow.of(EPOCH, null));
    }

    @Test
    public void testConstructorOfZeroLengthWindow() {
        // verify that this is doable
        assertDoesNotThrow(() -> TimeWindow.of(EPOCH, EPOCH));
    }

    @Test
    public void testStaticConstructor() {
        TimeWindow sample = TimeWindow.of(EPOCH, EPOCH.plusSeconds(1));

        assertEquals(EPOCH, sample.start());

        assertEquals(EPOCH.plusSeconds(1), sample.end());
    }

    @Test
    public void testContains() {
        TimeWindow instance = TimeWindow.of(EPOCH, EPOCH.plusSeconds(120));

        assertThat(instance.contains(EPOCH)).isTrue();
        assertThat(instance.contains(EPOCH.plusSeconds(120))).isTrue();
        assertThat(instance.contains(EPOCH.minusNanos(1))).isFalse();
        assertThat(instance.contains(EPOCH.plusSeconds(120).plusNanos(1))).isFalse();
    }

    @Test
    public void testLength() {
        TimeWindow original = TimeWindow.of(EPOCH, EPOCH.plusSeconds(120));

        assertEquals(120, original.duration().getSeconds());
        assertEquals(120 * 1000, original.duration().toMillis());
    }

    @Test
    public void testOverlapsWith() {

        TimeWindow all = TimeWindow.of(EPOCH, EPOCH.plusSeconds(120));
        TimeWindow firstHalf = TimeWindow.of(EPOCH, EPOCH.plusSeconds(60));
        TimeWindow secondHalf = TimeWindow.of(EPOCH.plusSeconds(60), EPOCH.plusSeconds(120));
        TimeWindow subset = TimeWindow.of(EPOCH.plusSeconds(1), EPOCH.plusSeconds(119));

        assertTrue(all.overlapsWith(firstHalf));
        assertTrue(firstHalf.overlapsWith(all));

        assertTrue(all.overlapsWith(secondHalf));
        assertTrue(secondHalf.overlapsWith(all));

        assertThat(secondHalf.overlapsWith(firstHalf)).isTrue();
        assertThat(firstHalf.overlapsWith(secondHalf)).isTrue();

        assertTrue(all.overlapsWith(subset));
        assertTrue(subset.overlapsWith(all));
    }

    @Test
    public void testGetOverlapWith() {

        TimeWindow all = TimeWindow.of(EPOCH, EPOCH.plusSeconds(120));
        TimeWindow firstHalf = TimeWindow.of(EPOCH, EPOCH.plusSeconds(60));
        TimeWindow secondHalf = TimeWindow.of(EPOCH.plusSeconds(60), EPOCH.plusSeconds(120));
        TimeWindow subset = TimeWindow.of(EPOCH.plusSeconds(1), EPOCH.plusSeconds(119));

        assertEquals(
                TimeWindow.of(EPOCH, EPOCH.plusSeconds(60)),
                all.getOverlapWith(firstHalf).get());
        assertEquals(
                TimeWindow.of(EPOCH, EPOCH.plusSeconds(60)),
                firstHalf.getOverlapWith(all).get());

        assertEquals(
                TimeWindow.of(EPOCH.plusSeconds(60), EPOCH.plusSeconds(120)),
                all.getOverlapWith(secondHalf).get());
        assertEquals(
                TimeWindow.of(EPOCH.plusSeconds(60), EPOCH.plusSeconds(120)),
                secondHalf.getOverlapWith(all).get());

        assertTrue(firstHalf.getOverlapWith(secondHalf).isPresent());
        assertTrue(secondHalf.getOverlapWith(firstHalf).isPresent());

        assertEquals(
                TimeWindow.of(EPOCH.plusSeconds(1), EPOCH.plusSeconds(119)),
                all.getOverlapWith(subset).get());
        assertEquals(
                TimeWindow.of(EPOCH.plusSeconds(1), EPOCH.plusSeconds(119)),
                subset.getOverlapWith(all).get());
    }

    @Test
    public void testBug16_timeWindowsThatShareAnEndpointShouldOverlap() {

        TimeWindow window1 = TimeWindow.of(EPOCH, EPOCH.plusSeconds(60));
        TimeWindow window2 = TimeWindow.of(EPOCH.plusSeconds(60), EPOCH.plusSeconds(120));

        // do this twice, changing argument order each time
        assertThat(window1.getOverlapWith(window2).isPresent()).isTrue();
        assertThat(window2.getOverlapWith(window1).isPresent()).isTrue();
        // do this twice, changing argument order each time
        assertThat(window1.getOverlapWith(window2).get().duration()).isEqualTo(Duration.ZERO);
        assertThat(window2.getOverlapWith(window1).get().duration()).isEqualTo(Duration.ZERO);
    }

    @Test
    public void testEquals() {

        TimeWindow item = TimeWindow.of(EPOCH, EPOCH.plusSeconds(1));
        TimeWindow copy = TimeWindow.of(EPOCH, EPOCH.plusSeconds(1));
        TimeWindow diff1 = TimeWindow.of(EPOCH, EPOCH.plusSeconds(2));
        TimeWindow diff2 = TimeWindow.of(EPOCH.minusSeconds(1), EPOCH.plusSeconds(1));

        assertTrue(item.equals(item));
        assertTrue(item.equals(copy));
        assertTrue(copy.equals(item));

        assertFalse(item.equals(diff1));
        assertFalse(diff1.equals(copy));

        assertFalse(item.equals(diff2));
        assertFalse(diff2.equals(copy));

        assertFalse(item.equals("not a TimeWindow"));

        assertFalse(item.equals(null));
    }

    @Test
    public void testHashcode() {

        TimeWindow item = TimeWindow.of(EPOCH, EPOCH.plusSeconds(1));
        TimeWindow copy = TimeWindow.of(EPOCH, EPOCH.plusSeconds(1));
        TimeWindow diff1 = TimeWindow.of(EPOCH, EPOCH.plusSeconds(2));
        TimeWindow diff2 = TimeWindow.of(EPOCH.minusSeconds(2), EPOCH.plusSeconds(1));

        assertTrue(item.hashCode() == copy.hashCode());
        assertFalse(item.hashCode() == diff1.hashCode());
        assertFalse(item.hashCode() == diff2.hashCode());
    }

    @Test
    public void testConsistencyBtwContainsAndOverlaps() {

        Instant time1 = EPOCH;
        Instant time2 = EPOCH.plusSeconds(30);
        Instant time3 = EPOCH.plusSeconds(60);

        TimeWindow window1 = TimeWindow.of(time1, time2);
        TimeWindow window2 = TimeWindow.of(time2, time3);

        assertTrue(window1.contains(time1));
        assertTrue(window1.contains(time2));

        assertTrue(window2.contains(time2));
        assertTrue(window2.contains(time3));

        assertThat(window1.overlapsWith(window2)).isTrue();
    }

    @Test
    public void testToFractionOfRange() {

        Instant time1 = EPOCH;
        Instant time2 = EPOCH.plusSeconds(30);

        TimeWindow window = TimeWindow.of(time1, time2);
        double TOLERANCE = 0.00001;

        assertEquals(window.toFractionOfRange(time1), 0.0, TOLERANCE);
        assertEquals(window.toFractionOfRange(time2), 1.0, TOLERANCE);
        assertEquals(window.toFractionOfRange(EPOCH.plusSeconds(15)), 0.5, TOLERANCE);

        assertEquals(window.toFractionOfRange(EPOCH.plusSeconds(45)), 1.5, TOLERANCE);
        assertEquals(window.toFractionOfRange(EPOCH.minusSeconds(45)), -1.5, TOLERANCE);
        assertEquals(window.toFractionOfRange(EPOCH.minusSeconds(30)), -1.0, TOLERANCE);
    }

    @Test
    public void testInstantWithin() {

        Instant time1 = EPOCH;
        Instant time2 = EPOCH.plusSeconds(30);

        TimeWindow window = TimeWindow.of(time1, time2);

        assertEquals(EPOCH, window.instantWithin(0.0));
        assertEquals(EPOCH.plusSeconds(30), window.instantWithin(1.0));
        assertEquals(EPOCH.plusSeconds(15), window.instantWithin(0.5));
    }

    @Test
    public void testInstantWithinOnBadInput() {

        TimeWindow window = TimeWindow.of(EPOCH, EPOCH.plusSeconds(30));

        // "no greater than 1"
        assertThrows(IllegalArgumentException.class, () -> window.instantWithin(1.1));

        // at least 0
        assertThrows(IllegalArgumentException.class, () -> window.instantWithin(-0.1));
    }

    @Test
    public void isEmpty_onZeroDurationWindow() {

        TimeWindow window = TimeWindow.of(EPOCH, EPOCH);

        assertThat(window.isEmpty()).isTrue();
    }

    @Test
    public void isEmpty_onNonZeroDurationWindow() {

        TimeWindow window = TimeWindow.of(EPOCH, EPOCH.plusMillis(1));

        assertThat(window.isEmpty()).isFalse();
    }

    @Test
    public void pad_increaseDurationByTwicePadding() {

        TimeWindow tw = TimeWindow.of(EPOCH, EPOCH.plusSeconds(10));
        TimeWindow padded = tw.pad(Duration.ofSeconds(2));

        assertThat(padded.duration()).isEqualTo(Duration.ofSeconds(14));
    }

    @Test
    public void shiftSlides() {
        TimeWindow window = TimeWindow.of(EPOCH, EPOCH.plusSeconds(11));
        Duration shiftAmount = Duration.ofSeconds(3);

        TimeWindow shifted = window.shift(shiftAmount);

        assertThat(shifted.duration()).isEqualTo(window.duration());
        assertThat(shifted.start()).isEqualTo(window.start().plus(shiftAmount));
        assertThat(shifted.end()).isEqualTo(window.end().plus(shiftAmount));
    }

    @Test
    public void bulkSlide() {
        ArrayList<TimeWindow> list = newArrayList(
                TimeWindow.of(EPOCH, EPOCH.plusSeconds(11)),
                TimeWindow.of(EPOCH.plusSeconds(1), EPOCH.plusSeconds(12)));

        ArrayList<TimeWindow> shifted = TimeWindow.shiftAll(list, Duration.ofSeconds(1000));

        assertThat(shifted.get(0).start()).isEqualTo(EPOCH.plusSeconds(1000));
        assertThat(shifted.get(1).start()).isEqualTo(EPOCH.plusSeconds(1001));

        assertThat(shifted.get(0).end()).isEqualTo(EPOCH.plusSeconds(1011));
        assertThat(shifted.get(1).end()).isEqualTo(EPOCH.plusSeconds(1012));
    }

    @Test
    public void shiftSlides_inMillis() {
        TimeWindow window = TimeWindow.of(EPOCH, EPOCH.plusSeconds(11));
        long shiftAmount = Duration.ofSeconds(3).toMillis();

        TimeWindow shifted = window.shiftMillis(shiftAmount);

        assertThat(shifted.duration()).isEqualTo(window.duration());
        assertThat(shifted.start()).isEqualTo(window.start().plusMillis(shiftAmount));
        assertThat(shifted.end()).isEqualTo(window.end().plusMillis(shiftAmount));
    }

    @Test
    public void bulkSlide_inMillis() {
        ArrayList<TimeWindow> list = newArrayList(
                TimeWindow.of(EPOCH, EPOCH.plusSeconds(11)),
                TimeWindow.of(EPOCH.plusSeconds(1), EPOCH.plusSeconds(12)));

        ArrayList<TimeWindow> shifted = TimeWindow.shiftAll(list, 1000);

        assertThat(shifted.get(0).start()).isEqualTo(EPOCH.plusSeconds(1));
        assertThat(shifted.get(1).start()).isEqualTo(EPOCH.plusSeconds(2));

        assertThat(shifted.get(0).end()).isEqualTo(EPOCH.plusSeconds(12));
        assertThat(shifted.get(1).end()).isEqualTo(EPOCH.plusSeconds(13));
    }
}

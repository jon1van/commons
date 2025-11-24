package io.github.jon1van.units;

import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    public void rejectsBadLatitudes() {
        assertThrows(IllegalArgumentException.class, () -> new Position(0L, 90.1, 0.0, 100.0));
    }

    @Test
    public void rejectsBadLongitudes() {
        assertThrows(IllegalArgumentException.class, () -> new Position(0L, 0.0, 180.1, 100.0));
    }

    @Test
    public void constructionMakesWhatWeExpect() {
        Position pos = new Position(0L, 1.0, 20.0, 150.0);

        assertThat(pos.time()).isEqualTo(EPOCH);
        assertThat(pos.latLong()).isEqualTo(LatLong.of(1.0, 20.0));
        assertThat(pos.altitude()).isEqualTo(Distance.ofFeet(150.0));
        assertThat(pos.hasAltitude()).isTrue();
    }

    @Test
    public void constructionMakesWhatWeExpect_2() {
        Position pos = new Position(EPOCH, LatLong.of(1.0, 20.0), Distance.ofFeet(150.0));

        assertThat(pos.time()).isEqualTo(EPOCH);
        assertThat(pos.latLong()).isEqualTo(LatLong.of(1.0, 20.0));
        assertThat(pos.altitude()).isEqualTo(Distance.ofFeet(150.0));
        assertThat(pos.hasAltitude()).isTrue();
    }

    @Test
    public void altitudeCanBeMissing() {
        Position pos = new Position(EPOCH, LatLong.of(1.0, 20.0));

        assertThat(pos.altitude()).isNull();
        assertThat(pos.hasAltitude()).isFalse();
    }

    @Test
    public void constructionThroughBuilderMakesWhatWeExpect() {
        Position pos = Position.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .build();

        assertThat(pos.time()).isEqualTo(EPOCH);
        assertThat(pos.latLong()).isEqualTo(LatLong.of(1.0, 20.0));
        assertThat(pos.altitude()).isEqualTo(Distance.ofFeet(150.0));
        assertThat(pos.hasAltitude()).isTrue();
    }

    @Test
    public void constructionThroughBuilderMakesWhatWeExpect_noAltitude() {
        Position pos = Position.builder().time(EPOCH).latLong(1.0, 20.0).build();

        assertThat(pos.time()).isEqualTo(EPOCH);
        assertThat(pos.latLong()).isEqualTo(LatLong.of(1.0, 20.0));
        assertThat(pos.altitude()).isNull();
        assertThat(pos.hasAltitude()).isFalse();
    }

    @Test
    public void builderWithSeedClones() {

        Position pos = Position.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .build();

        Position pos2 = Position.builder(pos).build();

        assertThat(pos.equals(pos2)).isTrue();
    }

    @Test
    public void builderWithSeedClones_removeAltitude() {

        Position pos = Position.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .build();

        Position pos2 = Position.builder(pos).butAltitude(null).build();

        assertThat(pos.time()).isEqualTo(pos2.time());
        assertThat(pos.latLong()).isEqualTo(pos2.latLong());
        assertThat(pos2.hasAltitude()).isFalse();
        assertThat(pos2.altitude()).isNull();
    }

    @Test
    public void failWhenBuilderSetsLatLongTwice() {
        assertThrows(
                IllegalStateException.class,
                () -> Position.builder().latLong(0.0, 0.0).latLong(1.0, 1.0));
    }

    @Test
    public void failWhenBuilderSetsLatLongTwice_2() {
        assertThrows(
                IllegalStateException.class,
                () -> Position.builder().latLong(LatLong.of(0.0, 0.0)).latLong(LatLong.of(0.0, 0.0)));
    }

    @Test
    public void failWhenBuilderSetsTimeTwice() {
        assertThrows(
                IllegalStateException.class,
                () -> Position.builder().time(EPOCH).time(EPOCH.plusSeconds(1L)));
    }

    @Test
    public void failWhenBuilderSetsAltitudeTwice() {
        assertThrows(
                IllegalStateException.class,
                () -> Position.builder().altitudeInFeet(150.0).altitudeInFeet(150.0));
    }

    @Test
    public void builderCanOverrideUsingButMethods() {

        Position pos = Position.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .build();

        Position pos2 = Position.builder(pos).butTime(EPOCH.plusSeconds(1L)).build();

        assertThat(pos2.time()).isEqualTo(EPOCH.plusSeconds(1L));
    }
}

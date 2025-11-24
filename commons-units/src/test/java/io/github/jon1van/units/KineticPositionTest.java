package io.github.jon1van.units;

import static io.github.jon1van.units.Speed.Unit.FEET_PER_MINUTE;
import static io.github.jon1van.units.Speed.Unit.KNOTS;
import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class KineticPositionTest {

    @Test
    public void constructorMakesWhatWeExpect() {
        KineticPosition pos = new KineticPosition(
                EPOCH,
                LatLong.of(1.0, 20.0),
                Distance.ofFeet(150.0),
                Speed.of(7, FEET_PER_MINUTE),
                Course.ofDegrees(12),
                1.0,
                Speed.of(42, KNOTS),
                Acceleration.of(Speed.ofKnots(22)));

        assertThat(pos.time()).isEqualTo(EPOCH);
        assertThat(pos.latLong()).isEqualTo(LatLong.of(1.0, 20.0));
        assertThat(pos.altitude()).isEqualTo(Distance.ofFeet(150.0));
        assertThat(pos.climbRate()).isEqualTo(Speed.of(7.0, FEET_PER_MINUTE));
        assertThat(pos.course()).isEqualTo(Course.ofDegrees(12));
        assertThat(pos.turnRate()).isEqualTo(1.0);
        assertThat(pos.speed()).isEqualTo(Speed.of(42.0, KNOTS));
        assertThat(pos.acceleration().speedDeltaPerSecond().inKnots()).isEqualTo(22.0);
    }

    @Test
    public void constructionThroughBuilderMakesWhatWeExpect() {
        KineticPosition pos = KineticPosition.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .climbRate(Speed.of(7, FEET_PER_MINUTE))
                .course(Course.ofDegrees(12))
                .turnRate(1.0)
                .speed(Speed.of(42, KNOTS))
                .acceleration(Acceleration.of(Speed.ofKnots(22)))
                .build();

        assertThat(pos.time()).isEqualTo(EPOCH);
        assertThat(pos.latLong()).isEqualTo(LatLong.of(1.0, 20.0));
        assertThat(pos.altitude()).isEqualTo(Distance.ofFeet(150.0));
        assertThat(pos.climbRate()).isEqualTo(Speed.of(7.0, FEET_PER_MINUTE));
        assertThat(pos.course()).isEqualTo(Course.ofDegrees(12));
        assertThat(pos.turnRate()).isEqualTo(1.0);
        assertThat(pos.speed()).isEqualTo(Speed.of(42.0, KNOTS));
        assertThat(pos.acceleration().speedDeltaPerSecond().inKnots()).isEqualTo(22.0);
    }

    @Test
    public void builderWithSeedClones() {

        KineticPosition pos = KineticPosition.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .climbRate(Speed.of(7, FEET_PER_MINUTE))
                .course(Course.ofDegrees(12))
                .turnRate(1.0)
                .speed(Speed.of(42, KNOTS))
                .acceleration(Acceleration.of(Speed.ofKnots(22.0)))
                .build();

        KineticPosition pos2 = KineticPosition.builder(pos).build();

        assertThat(pos.equals(pos2)).isTrue();
        assertThat(pos.hashCode()).isEqualTo(pos2.hashCode());
    }

    @Test
    public void failWhenBuilderSetsLatLongTwice() {
        assertThrows(
                IllegalStateException.class,
                () -> KineticPosition.builder().latLong(0.0, 0.0).latLong(1.0, 1.0));
    }

    @Test
    public void failWhenBuilderSetsLatLongTwice_2() {
        assertThrows(
                IllegalStateException.class,
                () -> KineticPosition.builder().latLong(LatLong.of(0.0, 0.0)).latLong(LatLong.of(0.0, 0.0)));
    }

    @Test
    public void failWhenBuilderSetsTimeTwice() {
        assertThrows(
                IllegalStateException.class,
                () -> KineticPosition.builder().time(EPOCH).time(EPOCH.plusSeconds(1L)));
    }

    @Test
    public void failWhenBuilderSetsAltitudeTwice() {
        assertThrows(
                IllegalStateException.class,
                () -> KineticPosition.builder().altitude(Distance.ofFeet(150.0)).altitude(Distance.ofFeet(150.0)));
    }

    @Test
    public void turnRateSpeedAndturnRadiusAgree() {

        KineticPosition pos = new KineticPosition(
                EPOCH,
                LatLong.of(1.0, 20.0),
                Distance.ofFeet(150.0),
                Speed.of(7, FEET_PER_MINUTE),
                Course.ofDegrees(12),
                1.0,
                Speed.of(42, KNOTS),
                Acceleration.of(Speed.ofKnots(22)));

        // turning 1 degree per second will require 360 seconds to travel in a circle...
        assertThat(pos.turnRate()).isEqualTo(1.0);
        assertThat(pos.speed()).isEqualTo(Speed.of(42.0, KNOTS));

        Distance circumference = pos.speed().times(Duration.ofSeconds(360));
        Distance radius = circumference.times(1.0 / (2.0 * Math.PI));

        assertThat(pos.turnRadius()).isEqualTo(radius);
    }

    @Test
    public void turnRadiusDoesNotFailWhenTurnRateIsZero() {

        KineticPosition pos = new KineticPosition(
                EPOCH,
                LatLong.of(1.0, 20.0),
                Distance.ofFeet(150.0),
                Speed.of(7, FEET_PER_MINUTE),
                Course.ofDegrees(12),
                0.0, // TURN RATE IS ZERO
                Speed.of(42, KNOTS),
                Acceleration.of(Speed.ofKnots(22)));

        assertThat(pos.turnRadius().inNauticalMiles()).isEqualTo(Double.POSITIVE_INFINITY);
    }

    @Test
    public void negativeTurnRatesProduceNegativeTurnRadius() {

        KineticPosition position1 = new KineticPosition(
                EPOCH,
                LatLong.of(1.0, 20.0),
                Distance.ofFeet(150.0),
                Speed.of(7, FEET_PER_MINUTE),
                Course.ofDegrees(12),
                -1.0,
                Speed.of(42, KNOTS),
                Acceleration.of(Speed.ofKnots(22)));

        KineticPosition position2 =
                KineticPosition.builder(position1).butTurnRate(1.0).build();

        assertThat(position1.turnRadius().isNegative()).isTrue();
        assertThat(position1.turnRadius().abs()).isEqualTo(position2.turnRadius());
    }

    @Test
    public void toBytesFromBytes_doesNotChangeData() {

        KineticPosition pos = new KineticPosition(
                EPOCH,
                LatLong.of(1.0, 20.0),
                Distance.ofFeet(150.0),
                Speed.of(7, FEET_PER_MINUTE),
                Course.ofDegrees(12),
                0.0, // TURN RATE IS ZERO
                Speed.of(42, KNOTS),
                Acceleration.of(Speed.ofKnots(22)));

        byte[] bytes = pos.toBytes();

        KineticPosition pos_2 = KineticPosition.fromBytes(bytes);

        assertThat(pos).isEqualTo(pos_2);
        assertThat(bytes).isEqualTo(pos_2.toBytes());
    }

    @Test
    public void toBase64FromBase64_doesNotChangeData() {

        KineticPosition pos = KineticPosition.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .climbRate(Speed.of(7, FEET_PER_MINUTE))
                .course(Course.ofDegrees(12))
                .turnRate(1.0)
                .speed(Speed.of(42, KNOTS))
                .acceleration(Acceleration.of(Speed.ofKnots(22)))
                .build();

        String base64 = pos.toBase64();

        KineticPosition pos_2 = KineticPosition.fromBase64(base64);

        assertThat(pos).isEqualTo(pos_2);
        assertThat(base64).isEqualTo(pos_2.toBase64());
    }

    @Test
    public void toBase64_yields96CharString() {

        KineticPosition pos = KineticPosition.builder()
                .time(EPOCH)
                .latLong(1.0, 20.0)
                .altitude(Distance.ofFeet(150.0))
                .climbRate(Speed.of(7, FEET_PER_MINUTE))
                .course(Course.ofDegrees(12))
                .turnRate(1.0)
                .speed(Speed.of(42, KNOTS))
                .acceleration(Acceleration.of(Speed.ofKnots(22)))
                .build();

        String base65Str = pos.toBase64();

        assertThat(base65Str.length()).isEqualTo(96);
    }
}

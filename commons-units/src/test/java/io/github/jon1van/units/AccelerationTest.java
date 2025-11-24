package io.github.jon1van.units;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class AccelerationTest {

    @Test
    public void perSecondNormalizationIsCorrect() {

        Speed input = Speed.ofKnots(10.0);

        Acceleration accel = Acceleration.of(input, Duration.ofSeconds(2L));

        assertThat(accel.speedDeltaPerSecond()).isEqualTo(Speed.ofKnots(5.0));
    }

    @Test
    public void timesChangesMagnitude() {
        Speed base = Speed.ofKnots(1.0);
        Acceleration accel = Acceleration.of(base, Duration.ofSeconds(1L));
        assertThat(accel.times(2.0)).isEqualTo(Acceleration.of(Speed.ofKnots(2.0), Duration.ofSeconds(1L)));
    }

    @Test
    public void lessThan_isCorrect() {

        Acceleration accel0 = Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L));
        Acceleration accel1 = Acceleration.of(Speed.ofKnots(0.9), Duration.ofSeconds(1L));

        assertThat(accel1.isLessThan(accel0)).isTrue();
        assertThat(accel0.isLessThan(accel1)).isFalse();
        assertThat(accel0.isLessThan(accel0)).isFalse();
    }

    @Test
    public void lessThanOrEqualTo_isCorrect() {

        Acceleration accel0 = Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L));
        Acceleration accel1 = Acceleration.of(Speed.ofKnots(0.9), Duration.ofSeconds(1L));

        assertThat(accel1.isLessThanOrEqualTo(accel0)).isTrue();
        assertThat(accel0.isLessThanOrEqualTo(accel1)).isFalse();
        assertThat(accel0.isLessThanOrEqualTo(accel0)).isTrue();
    }

    @Test
    public void greaterThan_isCorrect() {

        Acceleration accel0 = Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L));
        Acceleration accel1 = Acceleration.of(Speed.ofKnots(0.9), Duration.ofSeconds(1L));

        assertThat(accel1.isGreaterThan(accel0)).isFalse();
        assertThat(accel0.isGreaterThan(accel1)).isTrue();
        assertThat(accel0.isGreaterThan(accel0)).isFalse();
    }

    @Test
    public void greaterThanOrEqualTo_isCorrect() {

        Acceleration accel0 = Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L));
        Acceleration accel1 = Acceleration.of(Speed.ofKnots(0.9), Duration.ofSeconds(1L));

        assertThat(accel1.isGreaterThanOrEqualTo(accel0)).isFalse();
        assertThat(accel0.isGreaterThanOrEqualTo(accel1)).isTrue();
        assertThat(accel0.isGreaterThanOrEqualTo(accel0)).isTrue();
    }

    @Test
    public void signTestorsAreCorrect() {

        Acceleration posAccelertation = Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L));
        Acceleration negAcceleration = Acceleration.of(Speed.ofKnots(-1.0), Duration.ofSeconds(1L));
        Acceleration zeroAccel = Acceleration.of(Speed.ZERO, Duration.ofMinutes(20));

        assertThat(posAccelertation.isPositive()).isTrue();
        assertThat(posAccelertation.isNegative()).isFalse();
        assertThat(posAccelertation.isZero()).isFalse();

        assertThat(negAcceleration.isPositive()).isFalse();
        assertThat(negAcceleration.isNegative()).isTrue();
        assertThat(negAcceleration.isZero()).isFalse();

        assertThat(zeroAccel.isPositive()).isFalse();
        assertThat(zeroAccel.isNegative()).isFalse();
        assertThat(zeroAccel.isZero()).isTrue();
    }

    @Test
    public void abs_Works() {

        Acceleration posAccelertation = Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L));
        Acceleration negAcceleration = Acceleration.of(Speed.ofKnots(-1.0), Duration.ofSeconds(1L));

        assertThat(posAccelertation.abs()).isEqualTo(posAccelertation);
        assertThat(negAcceleration.abs()).isNotEqualTo(negAcceleration);
        assertThat(negAcceleration.abs()).isEqualTo(Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L)));
    }

    @Test
    public void equalsIsGood() {

        Acceleration accel0 = Acceleration.of(Speed.ofKnots(1.0), Duration.ofSeconds(1L));
        Acceleration accel1 = Acceleration.of(Speed.ofKnots(2.0), Duration.ofSeconds(2L));

        assertThat(accel0.equals(accel1)).isTrue();
        assertThat(accel0.hashCode()).isEqualTo(accel1.hashCode());
    }
}

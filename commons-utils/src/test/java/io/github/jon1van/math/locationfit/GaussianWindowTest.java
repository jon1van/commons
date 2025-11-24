package io.github.jon1van.math.locationfit;

import static java.time.Instant.EPOCH;
import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import io.github.jon1van.units.TimeWindow;
import org.junit.jupiter.api.Test;

public class GaussianWindowTest {

    @Test
    public void weightsAreNormalDist() {

        Duration windowSize = Duration.ofMinutes(1); // aka 6 standard deviations

        GaussianWindow gw = new GaussianWindow(windowSize);

        double plus6 = gw.computeGaussianWeight(EPOCH, EPOCH.plus(windowSize));
        double minus6 = gw.computeGaussianWeight(EPOCH, EPOCH.minus(windowSize));

        double plus3 = gw.computeGaussianWeight(EPOCH, EPOCH.plus(windowSize.dividedBy(2)));
        double minus3 = gw.computeGaussianWeight(EPOCH, EPOCH.minus(windowSize.dividedBy(2)));

        double plus1 = gw.computeGaussianWeight(EPOCH, EPOCH.plus(windowSize.dividedBy(6)));
        double minus1 = gw.computeGaussianWeight(EPOCH, EPOCH.minus(windowSize.dividedBy(6)));

        // "Weight for +6 standard deviations is essentially 0"
        assertThat(minus6).isLessThan(0.000001);
        // "Weight for -6 standard deviations is essentially 0"
        assertThat(plus6).isLessThan(0.000001);

        // 0.011108996538242 = Math.exp(-(3.0 * 3.0) / 2.0) = Expected Weight at +/1 3 Standard Dev
        assertThat(0.01 < plus3 && plus3 < 0.02).isTrue();
        assertThat(0.01 < minus3 && minus3 < 0.02).isTrue();

        // 0.606530659712633 = Math.exp(-(1.0 * 1.0) / 2.0) = Expected Weight at +/- 1 Standard Dev
        assertThat(0.60 < plus1 && plus1 < 0.61).isTrue();
        assertThat(0.60 < minus1 && minus1 < 0.61).isTrue();
    }

    @Test
    public void sigmaIsOneSixthWindowSize() {

        Duration windowSize = Duration.ofMinutes(2);

        GaussianWindow gw = new GaussianWindow(windowSize);

        Duration sigma = gw.sigma();

        assertThat(sigma.toMillis() * 6).isEqualTo(windowSize.toMillis());
    }

    @Test
    public void onDemandWindowsAreCorrectSizeAndLocation() {

        Duration windowSize = Duration.ofMinutes(2);

        GaussianWindow gw = new GaussianWindow(windowSize);

        // build an "onDemand TimeWindow we can use to filter data"
        TimeWindow epochWindow = gw.windowCenteredAt(EPOCH);

        assertThat(epochWindow.duration()).isEqualTo(windowSize);
        assertThat(epochWindow.start()).isEqualTo(EPOCH.minus(Duration.ofMinutes(1)));
        assertThat(epochWindow.end()).isEqualTo(EPOCH.plus(Duration.ofMinutes(1)));
    }

    @Test
    public void onDemandWindowsExcludeDataBeyondThreeSigma() {

        GaussianWindow gw = new GaussianWindow(Duration.ofMinutes(2));

        Duration sigma = gw.sigma();

        TimeWindow epochWindow = gw.windowCenteredAt(EPOCH);

        Instant justInside = EPOCH.plus(sigma.multipliedBy(3));
        Instant justOutside = EPOCH.plus(sigma.multipliedBy(3)).plusSeconds(1);

        assertThat(epochWindow.contains(justInside)).isTrue();
        assertThat(epochWindow.contains(justOutside)).isFalse();
    }
}

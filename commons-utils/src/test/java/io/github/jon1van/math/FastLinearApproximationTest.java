package io.github.jon1van.math;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class FastLinearApproximationTest {

    @Test
    public void errorGivenSlopeReflectsBasicTrend() {

        // this is a straight line with slope 0.5
        Double[] xData = new Double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        Double[] yData = new Double[] {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};

        List<Double> xDataList = newArrayList(xData);
        List<Double> yDataList = newArrayList(yData);

        FastLinearApproximation fla = new FastLinearApproximation(xDataList, yDataList);

        // the slope of the line is positive, so proposing a postive slop should generate a smaller error the a negative
        // slope
        assertThat(fla.sumSquaredErrorGivenSlope(1) < fla.sumSquaredErrorGivenSlope(-1))
                .isTrue();

        // the slope of the line is exactly 0.5 -- this error should be smallest
        assertThat(fla.sumSquaredErrorGivenSlope(0.5) < fla.sumSquaredErrorGivenSlope(0.49))
                .isTrue();
        assertThat(fla.sumSquaredErrorGivenSlope(0.5) < fla.sumSquaredErrorGivenSlope(0.51))
                .isTrue();
        assertThat(fla.sumSquaredErrorGivenSlope(0.5)).isEqualTo(0.0, within(0.001));

        assertThat(fla.averageY()).isEqualTo(2.75);
        assertThat(fla.slope()).isEqualTo(0.5, within(0.001));
        assertThat(fla.totalSquaredError()).isEqualTo(0.0, within(0.001));
    }

    @Test
    public void totalSquaredErrorComputedAsExpected() {

        // this is symmertric curve: straight from (1.0, 0.5) to (5.0, 2.5)...and then back down
        Double[] xData = new Double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        Double[] yData = new Double[] {0.5, 1.0, 1.5, 2.0, 2.5, 2.5, 2.0, 1.5, 1.0, 0.5};

        List<Double> xDataList = newArrayList(xData);
        List<Double> yDataList = newArrayList(yData);

        FastLinearApproximation fla = new FastLinearApproximation(xDataList, yDataList);

        assertThat(fla.averageY()).isEqualTo(1.5);
        assertThat(fla.slope()).isEqualTo(0.0, within(0.001));

        double expectedSquaredError = Math.pow(1.5 - 0.5, 2)
                + Math.pow(1.5 - 1.0, 2)
                + Math.pow(1.5 - 1.5, 2)
                + Math.pow(1.5 - 2.0, 2)
                + Math.pow(1.5 - 2.5, 2)
                + Math.pow(1.5 - 2.5, 2)
                + Math.pow(1.5 - 2.0, 2)
                + Math.pow(1.5 - 1.5, 2)
                + Math.pow(1.5 - 1.0, 2)
                + Math.pow(1.5 - 0.5, 2);

        assertThat(fla.totalSquaredError()).isEqualTo(expectedSquaredError, within(0.001));
    }

    @Test
    public void badInputIsRejected_notSorted() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new FastLinearApproximation(newArrayList(2.0, 1.0, 3.0), newArrayList(1.0, 2.0, 3.0)));
    }

    @Test
    public void badInputIsRejected_sizeMismatch() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new FastLinearApproximation(newArrayList(1.0, 2.0, 3.0), newArrayList(1.0, 2.0, 3.0, 4.0)));
    }
}

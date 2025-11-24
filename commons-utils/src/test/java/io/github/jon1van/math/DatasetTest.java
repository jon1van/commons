package io.github.jon1van.math;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DatasetTest {

    @Test
    public void testConstructor() {
        ArrayList<Double> xValues = newArrayList(1.0, 2.0, 3.0, 4.0);
        ArrayList<Double> yValues = newArrayList(1.0, 1.0, 1.0, 1.0);

        XyDataset data = new XyDataset(xValues, yValues);
        assertThat(data.xData()).isEqualTo(xValues);
        assertThat(data.yData()).isEqualTo(yValues);

        assertThat(data.size()).isEqualTo(4);
    }

    @Test
    public void testFitting() {

        ArrayList<Double> xValues = newArrayList(1.0, 2.0, 3.0, 4.0);
        ArrayList<Double> yValues = newArrayList(1.0, 1.0, 1.0, 1.0);

        XyDataset data = new XyDataset(xValues, yValues);

        assertThat(data.length()).isEqualTo(3.0);
        assertThat(data.approximateFit().averageY()).isEqualTo(1.0);
        assertThat(data.approximateFit().slope()).isEqualTo(0.0);
    }

    @Test
    public void testTakeDerivative() {
        ArrayList<Double> xValues = newArrayList(1.0, 2.0, 3.0, 4.0);
        ArrayList<Double> yValues = newArrayList(1.0, 1.0, 1.0, 5.0);

        XyDataset data = new XyDataset(xValues, yValues);
        XyDataset derivative = data.takeDerivative();

        assertThat(derivative.xData()).isEqualTo(xValues);

        List<Double> derivativeY = derivative.yData();

        assertThat(derivativeY.get(0)).isEqualTo(0.0);
        assertThat(derivativeY.get(1)).isEqualTo(0.0);
        assertThat(derivativeY.get(2)).isEqualTo(2.0);
        assertThat(derivativeY.get(3)).isEqualTo(4.0);
    }
}

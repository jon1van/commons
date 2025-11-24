package io.github.jon1van.math;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.math.DataSplitterTest.loadTestXYData;
import static io.github.jon1van.math.VisvalingamSimplifier.computeTriangleArea;
import static java.lang.Math.sin;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.github.jon1van.math.DataSplitterTest.ListPair;
import io.github.jon1van.utils.SingleUseTimer;
import org.junit.jupiter.api.Test;

public class VisvalingamSimplifierTest {

    @Test
    public void testComputeTriangleArea_pointsInALine() {
        double area = computeTriangleArea(new XyPoint(0.0, 0.0), new XyPoint(1.0, 1.0), new XyPoint(2.0, 2.0));
        assertThat(area).isEqualTo(0.0, within(0.00001));
    }

    @Test
    public void testComputeTriangleArea_pointsInATriangle() {
        double area = computeTriangleArea(new XyPoint(0.0, 0.0), new XyPoint(1.0, 1.0), new XyPoint(2.0, 0.0));
        assertThat(area).isEqualTo(1.0, within(0.00001));
    }

    @Test
    public void testComputeTriangleArea_pointsInATriangle_2() {
        double area = computeTriangleArea(new XyPoint(0.0, 0.0), new XyPoint(1.0, 10.0), new XyPoint(2.0, 0.0));
        assertThat(area).isEqualTo(10.0, within(0.00001));
    }

    @Test
    public void testComputeTriangleArea_pointsInALine_2() {
        double area = computeTriangleArea(new XyPoint(0.0, 675.0), new XyPoint(4.0, 675.0), new XyPoint(9.0, 675.0));
        assertThat(area).isEqualTo(0.0, within(0.00001));
    }

    @Test
    public void demoOnRealWorldExample_altitudeData() throws IOException {
        /* This is more of a demo than a test.  Use this test to compare input/output pairs. */

        ListPair allData = loadTestXYData(
                "io/github/jon1van/utils/math/altitudes1.txt"
                //			"org/mitre/caasd/commons/math/altitudes2.txt"
                //			"org/mitre/caasd/commons/math/altitudes3.txt"
                //			"org/mitre/caasd/commons/math/altitudes4.txt"
                );

        XyDataset inputData = new XyDataset(allData.first(), allData.second());

        //		System.out.println("Input data:");
        //		printPoints(dataset.asXyPointList());

        VisvalingamSimplifier simplifer = new VisvalingamSimplifier();

        XyDataset output = simplifer.simplify(inputData, 300 * 20); // 300 ft * 20 seconds

        //		System.out.println("Output Data");
        //		printPoints(output.asXyPointList());

        // Most of the data was removed during simplification
        assertThat(inputData.size()).isGreaterThan(output.size() * 10);
    }

    @Test
    public void canProcess100kPointsInLessThanOneSecond() {

        List<Double> x = incrementsOfOneThousanths(100_000);
        List<Double> y = sinX(x);
        XyDataset input = new XyDataset(x, y);

        Double THRESHOLD = 0.05;

        SingleUseTimer timer = new SingleUseTimer();
        timer.tic();

        XyDataset output = (new VisvalingamSimplifier()).simplify(input, THRESHOLD);

        timer.toc();
        Duration elapsedTime = timer.elapsedTime();

        //        "This operation should be quick -- even though it had 100k points",
        assertThat(elapsedTime.toMillis()).isLessThan(1_000);

        // The output curve should have fewer than 1% of the original points
        assertThat(output.size() * 100 < input.size()).isTrue();

        XyPoint left = null;
        XyPoint center = null;
        XyPoint right = null;

        for (XyPoint xyPoint : output.asXyPointList()) {
            left = center;
            center = right;
            right = xyPoint;

            if (nonNull(left) && nonNull(center) && nonNull(right)) {
                assertThat(computeTriangleArea(left, center, right)).isGreaterThan(THRESHOLD);
            }
        }

        // print data
        // printPoints(output.asXyPointList());
    }

    private void printPoints(Collection<XyPoint> points) {
        points.forEach(xypoint -> printPoint(xypoint));
    }

    private void printPoint(XyPoint point) {
        System.out.println(point.x + "\t" + point.y);
    }

    private List<Double> incrementsOfOneThousanths(int n) {

        ArrayList<Double> x = newArrayList();
        for (double i = 0; i < n; i++) {
            x.add(i * 0.001);
        }
        return x;
    }

    // gives sin wave...
    private List<Double> sinX(List<Double> xData) {
        return xData.stream().map(x -> sin(x)).collect(toCollection(ArrayList::new));
    }
}

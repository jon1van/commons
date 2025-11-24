package io.github.jon1van.math;

import static io.github.jon1van.math.DataSplitterTest.loadTestXYData;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.stream.Stream;

import io.github.jon1van.math.DataSplitterTest.ListPair;
import org.junit.jupiter.api.Test;

public class VisvalingamSplitterTest {

    @Test
    public void realSampleDataIsSplitWell() throws IOException {
        ListPair allData = loadTestXYData("io/github/jon1van/utils/math/altitudes1.txt");

        XyDataset inputData = new XyDataset(allData.first(), allData.second());

        XyDataset[] outputDatasets =
                (new VisvalingamSplitter(300 * 20)).split(inputData); // 300 feet of error over 20 seconds

        XyDataset directSimplification = (new VisvalingamSimplifier()).simplify(inputData, 300 * 20);

        int totalOutputSize =
                Stream.of(outputDatasets).mapToInt(dataset -> dataset.size()).sum();

        assertThat(inputData.size()).isEqualTo(totalOutputSize);

        // the first point in each output partition is found in the results from directly applying a
        // VisvalingamSimplifier
        for (XyDataset outputDataset : outputDatasets) {
            Double firstX = outputDataset.xData().get(0);
            assertThat(directSimplification.xData().contains(firstX)).isTrue();
        }

        // There is one output partition for each point identied by the VisvalingamSimplifier
        assertThat(directSimplification.size()).isEqualTo(outputDatasets.length + 1);
    }
}

package io.github.jon1van.math;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

/// A DataSplitter partitions an XY dataset into multiple chunks based on some criterion.
///
/// One implementation could chop a dataset into multiple piece-wise linear segments. Another
/// implementation might split a dataset when two consecutive data points are too far apart.
@FunctionalInterface
public interface DataSplitter {

    int[] computeSplitsFor(List<Double> xData, List<Double> yData);

    default int[] computeSplitsFor(XyDataset dataset) {
        return computeSplitsFor(dataset.xData(), dataset.yData());
    }

    default XyDataset[] split(XyDataset dataset) {
        return dataset.split(this);
    }

    /// Reusable input check method for all DataSplitters. Confirms, (1) inputs are not null, (2)
    /// inputs have same size, (3) the x data is strictly increasing.
    ///
    /// @param xData A strictly increasing sequence of x values
    /// @param yData A y value for each x value
    static void checkInputData(List<Double> xData, List<Double> yData) {
        checkNotNull(xData);
        checkNotNull(yData);
        checkArgument(xData.size() == yData.size(), "The xData and yData have different sizes");
        checkOrdering(xData);
    }

    /// Throw an IllegalArgumentException if the xData is not strictly increasing.
    ///
    /// @param xData A sequence of strictly increasing values.
    static void checkOrdering(List<Double> xData) {

        double last = Double.NEGATIVE_INFINITY;
        for (Double cur : xData) {
            if (cur <= last) {
                throw new IllegalArgumentException("The input is not sorted");
            }
            last = cur;
        }
    }
}

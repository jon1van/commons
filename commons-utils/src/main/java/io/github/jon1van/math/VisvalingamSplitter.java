package io.github.jon1van.math;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Ints;

/// A VisvalingamSplitter partitions a Dataset by using the "simplified" Dataset provided by a
/// VisvalingamSimplifier.
public class VisvalingamSplitter implements DataSplitter {

    private final VisvalingamSimplifier simplifier;

    private final double IMPORTANT_TRIANGLE_THRESHOLD;

    /// Create a DataSplitter that uses the points identified by Visvalingam's Algorithm (via a
    /// VisvalingamSimplifier) to partition the data.
    ///
    /// @param importantTriangleThreshold Choose the value based on the dimensions of the input X and
    ///                                   Y data. The smaller this threshold is the finer the
    ///                                   resulting splits are (i.e. the VisvalingamSimplifier
    ///                                   retains more detail so more splits are found). If this
    ///                                   value is set high enough no splits will be found.
    public VisvalingamSplitter(double importantTriangleThreshold) {
        checkArgument(importantTriangleThreshold >= 0.0);
        this.simplifier = new VisvalingamSimplifier();
        this.IMPORTANT_TRIANGLE_THRESHOLD = importantTriangleThreshold;
    }

    @Override
    public int[] computeSplitsFor(List<Double> xData, List<Double> yData) {

        XyDataset data = new XyDataset(xData, yData);

        // identifies the most "visually important" points in the graph
        XyDataset keyPoints = simplifier.simplify(data, IMPORTANT_TRIANGLE_THRESHOLD);

        ArrayList<Integer> indicesOfKeyPoints =
                keyPoints.xData().stream().map(xValue -> xData.indexOf(xValue)).collect(toCollection(ArrayList::new));

        /*
         * Increment the very last index value so you don't drop the very last piece of data.
         *
         * The goal is to support calls like "xData.subList(result[i], result[i+1])"
         */
        int n = indicesOfKeyPoints.size();
        int prior = indicesOfKeyPoints.get(n - 1);
        indicesOfKeyPoints.set(n - 1, prior + 1);

        return Ints.toArray(indicesOfKeyPoints);
    }
}

package io.github.jon1van.math;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.jon1van.math.DataSplitter.checkInputData;
import static io.github.jon1van.math.DataSplitter.checkOrdering;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import io.github.jon1van.utils.FileUtils;
import org.junit.jupiter.api.Test;

public class DataSplitterTest {

    public record ListPair(List<Double> first, List<Double> second) {}

    /// Load some test XY data (altitude over time)
    ///
    /// @return Two lists that can easily be used via the DataSplitter interface
    /// @throws IOException (if the target data can't be found)
    public static ListPair loadTestXYData(String filename) throws IOException {
        File file = FileUtils.getResourceFile(filename);
        List<String> lines = Files.readAllLines(file.toPath());
        List<Double> xValues = newArrayList();
        List<Double> yValues = newArrayList();
        for (String line : lines) {
            String[] values = line.split("\t");
            double x = Double.parseDouble(values[0]);
            double y = Double.parseDouble(values[1]);
            xValues.add(x);
            yValues.add(y);
        }
        return new ListPair(xValues, yValues);
    }

    @Test
    public void checkInputRejectsNullInputs_x() {

        assertThrows(NullPointerException.class, () -> checkInputData(null, newArrayList(2.0, 3.0)));
    }

    @Test
    public void checkInputRejectsNullInputs_y() {
        assertThrows(NullPointerException.class, () -> checkInputData(newArrayList(2.0, 3.0), null));
    }

    @Test
    public void checkInputRejectsInputWithDifferentSizes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> checkInputData(newArrayList(2.0, 3.0), newArrayList(2.0, 3.0, 4.0)));
    }

    @Test
    public void checkInputRejectsUnsortedXValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> checkInputData(
                        newArrayList(2.0, 3.0, -10.0), // x values must be sorted
                        newArrayList(2.0, 3.0, 4.0)));
    }

    @Test
    public void checkInputDoesNothingWhenInputIsGood() {
        checkInputData(newArrayList(2.0, 3.0, 4.0), newArrayList(20.0, -10.0, 400.0));
    }

    @Test
    public void checkOrderingDoesNothingWhenInputIsOrdered() {
        checkOrdering(newArrayList(1.0, 2.0, 3.0));
    }

    @Test
    public void checkOrderingRejectsUnsortedData() {
        assertThrows(IllegalArgumentException.class, () -> checkOrdering(newArrayList(10.0, 2.0, 11.0)));
    }

    @Test
    public void checkOrderingRejectsUnsortedData_duplicateValues() {
        // 2 copies of the same value should fail
        assertThrows(IllegalArgumentException.class, () -> checkOrdering(newArrayList(1.0, 2.0, 3.0, 3.0)));
    }
}

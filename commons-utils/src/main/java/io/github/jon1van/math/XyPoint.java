package io.github.jon1van.math;

import java.util.ArrayList;
import java.util.Collection;

public class XyPoint {

    final double x;
    final double y;

    public XyPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static XyPoint of(double x, double y) {
        return new XyPoint(x, y);
    }

    public static XyDataset asDataset(Collection<XyPoint> xyData) {
        // re-package results as a Dataset and return
        ArrayList<Double> xData = new ArrayList<>(xyData.size());
        ArrayList<Double> yData = new ArrayList<>(xyData.size());
        for (XyPoint xyPoint : xyData) {
            xData.add(xyPoint.x());
            yData.add(xyPoint.y());
        }
        return new XyDataset(xData, yData);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + " , " + y + ")";
    }
}

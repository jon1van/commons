package io.github.jon1van.math;

public class XyzPoint {

    final double x;
    final double y;
    final double z;

    public XyzPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static XyzPoint of(double x, double y, double z) {
        return new XyzPoint(x, y, z);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    @Override
    public String toString() {
        return "(" + x + " , " + y + " , " + z + ")";
    }
}

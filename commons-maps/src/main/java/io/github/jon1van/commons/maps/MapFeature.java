package io.github.jon1van.commons.maps;

import java.awt.Graphics2D;

/// A MapFeature is a dot, line, (curve?) that can be draw on a map.  The goal of MapFeature is
public interface MapFeature {

    /// Draw this MapFeature onto the Graphics2D.  Use the zeroPixel to place to drawing.
    void drawOn(Graphics2D g, PixelLatLong zeroPixel);
}

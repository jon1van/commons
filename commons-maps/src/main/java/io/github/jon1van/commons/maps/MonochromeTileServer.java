package io.github.jon1van.commons.maps;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

/// This "TileServer" returns Map Tile images that are a solid color.
///
/// Simple tiles like this can be useful when you just want a plain background color.
///
/// Simple tiles like this can be useful when writing unit tests that (A) run quickly because they
/// don't access remote assets or the file system and (B) are perfectly repeatable because the images
/// returned do not slowly change as the underlying map data is updated from time to time.
public class MonochromeTileServer implements TileServer {

    private final Color color;

    private final int tileSize = 512;

    public MonochromeTileServer(Color color) {
        requireNonNull(color);
        this.color = color;
    }

    @Override
    public int maxZoomLevel() {
        return 20;
    }

    @Override
    public int maxTileSize() {
        return 512;
    }

    @Override
    public URL getUrlFor(TileAddress ta) {
        throw new UnsupportedOperationException(
                "URLs are not provided because single color tiles can be created directly");
    }

    /// This method doesn't actually "download" a Map from a remote resource.  It generates the
    /// BufferedImage on demand
    ///
    /// @param tile This TileAddress is ignored (and only provided due to interface definition)
    /// @return A BufferedImage in which every pixel is set to the same color
    @Override
    public BufferedImage downloadMap(TileAddress tile) {

        BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, tileSize, tileSize);

        return img;
    }
}

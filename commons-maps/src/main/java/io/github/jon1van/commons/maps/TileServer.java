package io.github.jon1van.commons.maps;

import static com.google.common.base.Preconditions.checkArgument;
import static io.github.jon1van.commons.maps.TileAddress.cornerFinder;
import static io.github.jon1van.utils.DemotedException.demote;
import static java.util.Objects.requireNonNull;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

import io.github.jon1van.units.LatLong;

public interface TileServer {

    int maxZoomLevel();

    int maxTileSize();

    URL getUrlFor(TileAddress ta);

    default BufferedImage downloadMap(TileAddress ta) {

        URL url = getUrlFor(ta);

        // Todo -- log this download...

        try {
            InputStream inStream = url.openConnection().getInputStream();
            return ImageIO.read(inStream);
        } catch (IOException e) {
            throw demote("Could not query: " + url.getQuery(), e);
        }
    }

    /// Determine which Tile contains the provided LatLong, get a URL for that Tile, then open a
    /// connection to the provided URL, then parse out an Image from the InputStream found.
    default BufferedImage downloadMap(LatLong pointInTile, int zoom) throws IOException {
        TileAddress tile = TileAddress.of(pointInTile, zoom);
        return downloadMap(tile);
    }

    default BufferedImage downloadAndCombineTiles(List<TileAddress> tiles) {
        requireNonNull(tiles);
        checkArgument(!tiles.isEmpty());

        TileAddress topLeftTile = tiles.stream().min(cornerFinder()).get();
        TileAddress bottomRightTile = tiles.stream().max(cornerFinder()).get();

        int size = maxTileSize();

        BufferedImage combined = new BufferedImage(
                size * (bottomRightTile.xIndex() - topLeftTile.xIndex() + 1),
                size * (bottomRightTile.yIndex() - topLeftTile.yIndex() + 1),
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D) combined.getGraphics();

        int xOffset = topLeftTile.xIndex();
        int yOffset = topLeftTile.yIndex();

        for (TileAddress tile : tiles) {
            BufferedImage img = downloadMap(tile); // these queries are launch in serial...!
            // compute the "row/column" indices used to assemble the combined image
            int imgCol = tile.xIndex() - xOffset;
            int imgRow = tile.yIndex() - yOffset;
            g.drawImage(img, imgCol * size, imgRow * size, size, size, null);
        }

        return combined;
    }
}

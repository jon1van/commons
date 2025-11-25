package io.github.jon1van.maps;

import java.time.Duration;

import io.github.jon1van.units.LatLong;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class LocallyCachingTileServerTest {

    @Disabled
    @Test
    public void cacheWorks() throws Exception {

        LocallyCachingTileServer tileServer = new LocallyCachingTileServer(new DebugTileServer());

        tileServer.downloadMap(LatLong.of(32.8968, -97.0380), 10);
    }

    @Test
    public void cacheWithSmallLifetimeWorks() throws Exception {

        LocallyCachingTileServer tileServer =
                new LocallyCachingTileServer(new DebugTileServer(), Duration.ofSeconds(1L));

        tileServer.downloadMap(LatLong.of(32.8968, -97.0380), 10);
    }
}

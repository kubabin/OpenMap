package dev.kubabin.openmap.datasource;

import dev.kubabin.openmap.api.markers.Marker;

import java.util.stream.Stream;

public interface MarkerSource {
    Stream<Marker> getMarkers();
}

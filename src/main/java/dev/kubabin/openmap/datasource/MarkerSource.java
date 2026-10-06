package dev.kubabin.openmap.datasource;

import dev.kubabin.openmap.api.markers.Marker;
import net.minecraft.network.chat.Component;

import java.util.stream.Stream;

public interface MarkerSource {
    Stream<Marker> getMarkers();
    Component getName();
}

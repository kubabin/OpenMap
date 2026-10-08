package dev.kubabin.openmap.datasource;

import dev.kubabin.openmap.api.markers.Marker;
import dev.kubabin.openmap.waypoints.WaypointClientStorage;
import dev.kubabin.openmap.waypoints.WaypointMarker;
import net.minecraft.network.chat.Component;

import java.util.stream.Stream;

public class WaypointSource implements MarkerSource{
    @Override
    public Stream<Marker> getMarkers() {
        return WaypointClientStorage.getWaypoints().stream().map(WaypointMarker::new);
    }

    @Override
    public Component getName() {
        return Component.translatable("key.openmap.waypoints");
    }
}

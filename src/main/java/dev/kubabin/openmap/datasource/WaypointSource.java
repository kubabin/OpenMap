package dev.kubabin.openmap.datasource;

import dev.kubabin.openmap.api.markers.IconMarker;
import dev.kubabin.openmap.api.markers.Marker;
import dev.kubabin.openmap.waypoints.WaypointClientStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.stream.Stream;

public class WaypointSource implements MarkerSource{
    @Override
    public Stream<Marker> getMarkers() {
        return WaypointClientStorage.getWaypoints().stream().map(waypoint -> {
            IconMarker marker = new IconMarker(ResourceLocation.parse(waypoint.icon()));
            marker.x = waypoint.x();
            marker.y = waypoint.z();
            marker.scale = 0.5;
            marker.tooltip = Component.literal(waypoint.name());
            return marker;
        });
    }

    @Override
    public Component getName() {
        return Component.translatable("key.openmap.waypoints");
    }
}

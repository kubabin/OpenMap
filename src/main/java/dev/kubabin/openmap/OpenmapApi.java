package dev.kubabin.openmap;

import dev.kubabin.openmap.api.MenuItem;
import dev.kubabin.openmap.layers.LayerProvider;
import dev.kubabin.openmap.waypoints.Waypoint;
import dev.kubabin.openmap.waypoints.networking.CreateWaypointPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class OpenmapApi {
    protected static final HashMap<String, LayerProvider> layers = new HashMap<>();
    public static final HashMap<String, MenuItem> globalMenu = new HashMap<>();
    public static void addLayer(String key, LayerProvider layer){
        layers.put(key, layer);
    }
    public static LayerProvider getLayer(String key){
        return layers.get(key);
    }
    public static void addWaypoint(Waypoint waypoint){
        PacketDistributor.sendToServer(
                CreateWaypointPayload.from(waypoint)
        );
    }
}

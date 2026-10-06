package dev.kubabin.openmap.api;

import dev.kubabin.openmap.layers.LayerProvider;
import dev.kubabin.openmap.sidebuttons.SideButton;
import dev.kubabin.openmap.tools.MapTool;
import dev.kubabin.openmap.waypoints.Waypoint;
import dev.kubabin.openmap.waypoints.WaypointClientStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class OpenmapApi {
    public static final ArrayList<LayerProvider> layers = new ArrayList<>();
    public static final HashMap<String, MenuItem> globalMenu = new HashMap<>();
    public static final HashMap<String, SideButton> topSideButtons = new HashMap<>();
    public static final HashMap<String, MapTool> mapTools = new HashMap<>();
    public static final HashMap<String, SideButton> bottomSideButtons = new HashMap<>();
    public static void addLayer(LayerProvider layer){
        layers.add(layer);
    }
    public static void addWaypoint(Waypoint waypoint){
        WaypointClientStorage.addWaypoint(waypoint);
    }
}

package dev.kubabin.openmap.api;

import dev.kubabin.openmap.waypoints.Waypoint;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OpenmapApi {
    private static final HashMap<String, Marker> markers = new HashMap<>();
    public static final HashMap<String, MenuItem> globalMenu = new HashMap<>();
    public static final List<Waypoint> waypoints = new ArrayList<Waypoint>();
    public static HashMap<String, Marker> getMarkers(){
        return markers;
    }
    public static void addMarker(String key, Marker marker){
        markers.put(key,marker);
    }
    public static Marker getMarker(String key){
        return markers.get(key);
    }
}

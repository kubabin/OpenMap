package dev.kubabin.openmap;

import dev.kubabin.openmap.api.MenuItem;
import dev.kubabin.openmap.layers.LayerProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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
}

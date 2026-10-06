package dev.kubabin.openmap.compat;

import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.layers.LayerProvider;
import dev.kubabin.openmap.layers.TrainLayer;
import net.neoforged.fml.ModList;

public class CreateCompat {
    private static final String MODID = "create";
    public static void init(){
        if (!ModList.get().isLoaded(MODID)) return;
        LayerProvider trainLayer = new TrainLayer();

        OpenmapApi.addLayer(trainLayer);
    }

}

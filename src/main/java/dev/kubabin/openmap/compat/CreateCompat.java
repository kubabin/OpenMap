package dev.kubabin.openmap.compat;

import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.layers.LayerProvider;
import dev.kubabin.openmap.layers.TrainLayer;

public class CreateCompat {
    public static final String MODID = "create";
    public static void init(){
        LayerProvider trainLayer = new TrainLayer();

        OpenmapApi.addLayer(trainLayer);
    }

}

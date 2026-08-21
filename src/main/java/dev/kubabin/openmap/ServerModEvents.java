package dev.kubabin.openmap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import static dev.kubabin.openmap.Openmap.MODID;

@EventBusSubscriber(modid = MODID)
public class ServerModEvents {
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event){
    }
}

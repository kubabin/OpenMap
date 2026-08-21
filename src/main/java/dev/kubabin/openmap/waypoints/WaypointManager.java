package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.Openmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = Openmap.MODID)
public class WaypointManager {
    public WaypointManager(){}
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event){
        if (event.getLevel().isClientSide()) return;
    }

}

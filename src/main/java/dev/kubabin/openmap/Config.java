package dev.kubabin.openmap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Openmap.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.IntValue mapSizeConfig = BUILDER.defineInRange("mapSize", 128, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue rotateMapConfig = BUILDER
            .comment("Whether the minimap rotates with the player's facing direction")
            .define("rotateMap", false);
    private static final ModConfigSpec.BooleanValue circularMapConfig = BUILDER
            .comment("Whether the minimap is a circle or a square")
            .define("circularMap", true);
    private static final ModConfigSpec.BooleanValue deathWaypointsConfig = BUILDER
            .comment("Should a waypoint be created on death")
            .define("deathWaypoints", false);
    static final ModConfigSpec SPEC = BUILDER.build();

    private static int mapSize = 128;
    public static boolean rotateMap = false;
    public static boolean circularMap = true;
    public static boolean deathWaypoints = false;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        mapSize = mapSizeConfig.get();
        rotateMap = rotateMapConfig.get();
        circularMap = circularMapConfig.get();
        DynamicTextureManager.reinitTexture();

        deathWaypoints = deathWaypointsConfig.get();
    }
    static int getMapSize(){
        return Config.mapSize;
    }
    static int getBaseMapSize(){
        return Config.mapSize;
    }
}

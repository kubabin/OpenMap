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
    private static final ModConfigSpec.BooleanValue showMinimapConfig = BUILDER
            .comment("Whether to show the minimap")
            .define("showMinimap", true);
    private static final ModConfigSpec.BooleanValue renderChunkBordersConfig = BUILDER
            .comment("Whether to render chunk borders on the minimap")
            .define("renderChunkBorders", false);
    private static final ModConfigSpec.BooleanValue underwaterHillshadingConfig = BUILDER
            .comment("Whether to apply hillshading underwater")
            .define("underwaterHillshading", true);
    private static final ModConfigSpec.BooleanValue nightTintConfig = BUILDER
            .comment("Should the map get darker at night")
            .define("nightTint", true);
    private static final ModConfigSpec.DoubleValue maximumZoomoutConfig = BUILDER
            .comment("Maximum zoom out level for the minimap")
            .defineInRange("maximumZoomout", 0.3, 0.0, Double.MAX_VALUE);
    private static final ModConfigSpec.DoubleValue maximumEntityDistanceConfig = BUILDER
            .comment("Maximum vertical distance for entities to be rendered on the map")
            .defineInRange("maximumEntityDistance", 128.0, 0.0, Double.MAX_VALUE);
    static final ModConfigSpec SPEC = BUILDER.build();

    private static int mapSize = 128;
    public static boolean rotateMap = false;
    public static boolean circularMap = true;
    public static boolean deathWaypoints = false;
    public static boolean showMinimap = true;
    public static boolean renderChunkBorders = false;
    public static boolean underwaterHillshading = true;
    public static boolean nightTint = true;
    public static double maximumZoomout = 0.3;
    public static double maximumEntityDistance = 128.0;
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        mapSize = mapSizeConfig.get();
        rotateMap = rotateMapConfig.get();
        circularMap = circularMapConfig.get();
        DynamicTextureManager.reinitTexture();

        deathWaypoints = deathWaypointsConfig.get();
        showMinimap = showMinimapConfig.get();
        renderChunkBorders = renderChunkBordersConfig.get();
        underwaterHillshading = underwaterHillshadingConfig.get();
        nightTint = nightTintConfig.get();
        maximumZoomout = maximumZoomoutConfig.get();
        maximumEntityDistance = maximumEntityDistanceConfig.get();
    }
    static int getMapSize(){
        return Config.mapSize;
    }
    static int getBaseMapSize(){
        return Config.mapSize;
    }
}

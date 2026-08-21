package dev.kubabin.openmap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Openmap.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.IntValue mapSizeConfig = BUILDER.defineInRange("mapSize", 512, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue rotateMapConfig = BUILDER
            .comment("Whether the minimap rotates with the player's facing direction")
            .define("rotateMap", false);
    private static final ModConfigSpec.BooleanValue circularMapConfig = BUILDER
            .comment("Whether the minimap is masked to a circle instead of a square")
            .define("circularMap", true);
    static final ModConfigSpec SPEC = BUILDER.build();

    public static int mapSize = 512;
    public static boolean rotateMap = false;
    public static boolean circularMap = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        mapSize = mapSizeConfig.get();
        rotateMap = rotateMapConfig.get();
        circularMap = circularMapConfig.get();
    }
}

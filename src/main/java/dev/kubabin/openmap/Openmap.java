package dev.kubabin.openmap;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Openmap.MODID)
public class Openmap {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "openmap";
    // Directly reference a slf4j logger
    protected static final Logger LOGGER = LogUtils.getLogger();

    public static final String LAYER_WAYPOINTS = "key.openmap.waypoints";
    public static final String LAYER_PLAYERS   = "key.openmap.players";
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Openmap(ModContainer modContainer) {
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}

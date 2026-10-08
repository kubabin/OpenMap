package dev.kubabin.openmap;

import com.mojang.blaze3d.platform.InputConstants;
import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.compat.CreateCompat;
import dev.kubabin.openmap.compat.OpacCompat;
import dev.kubabin.openmap.datasource.EntitySource;
import dev.kubabin.openmap.datasource.PlayerSource;
import dev.kubabin.openmap.datasource.WaypointSource;
import dev.kubabin.openmap.layers.StreamedLayer;
import dev.kubabin.openmap.menuitems.CreateWaypointMenuItem;
import dev.kubabin.openmap.menuitems.TeleportMenuItem;
import dev.kubabin.openmap.sidebuttons.CenterPlayerButton;
import dev.kubabin.openmap.waypoints.Waypoint;
import dev.kubabin.openmap.waypoints.WaypointClientStorage;
import dev.kubabin.openmap.waypoints.networking.WaypointSyncPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.util.UUID;

import static dev.kubabin.openmap.CachedTile.getSessionIdentifier;
import static dev.kubabin.openmap.Openmap.MODID;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientModEvents {
    public static boolean cache_enabled = true;
    public static final Lazy<KeyMapping> toggleMapKey = Lazy.of(() -> new KeyMapping(
            "key.openmap.worldmap",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.misc"
    ));
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        event.enqueueWork(DynamicTextureManager::initTexture);
        // Set up tiles cache directory
        try {
            Files.createDirectories(CachedTile.DATA_DIR);
        } catch (Exception e) {
            cache_enabled = false;
            Openmap.LOGGER.warn("Couldn't create openmap_tiles_cache directory: {}", e.getMessage());
        }
        if (ModList.get().isLoaded(OpacCompat.OPAC_MODID)) {
            OpacCompat.init();
        }

        OpenmapApi.addLayer(new StreamedLayer(new EntitySource()));

        if (ModList.get().isLoaded(CreateCompat.MODID)) {
            CreateCompat.init();
        }

        OpenmapApi.addLayer(new StreamedLayer(new WaypointSource()));
        OpenmapApi.addLayer(new StreamedLayer(new PlayerSource()));

        // Built-in Global Menu Items
        OpenmapApi.globalMenu.put("teleport", TeleportMenuItem.createMenuItem());
        OpenmapApi.globalMenu.put("add-waypoint", CreateWaypointMenuItem.createMenuItem());
        OpenmapApi.topSideButtons.put("center-player", new CenterPlayerButton());

    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(toggleMapKey.get());
    }

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        // Call the newRender method to render the minimap
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(MODID, "minimap_hud"),
                (MinimapRendering::renderMinimap)
        );
    }


    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event){
        Minecraft mc = Minecraft.getInstance();

        WaypointClientStorage.isClientSide = !mc.hasSingleplayerServer() &&
                !event.getPlayer().connection.hasChannel(WaypointSyncPayload.TYPE);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){
        while (toggleMapKey.get().consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen(new WorldmapScreen());
            } else if (mc.screen instanceof WorldmapScreen) {
                mc.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event){
        if (event.getEntity() != Minecraft.getInstance().player) return;
        CachedTile.worldName = CachedTile.getSafeFolderName(getSessionIdentifier());
        CachedTile.ensureLevelDir((ClientLevel) event.getLevel());
        WaypointClientStorage.loadWaypoints();
        MapThread.startThread();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event){
        if (!event.getLevel().isClientSide()) return;
        MapThread.stop();
        MapThread.tileStorage.cleanup();
        WaypointClientStorage.saveWaypoints();
    }

    @SubscribeEvent
    public static void onClientPause(ClientPauseChangeEvent.Post event){
        if (event.isPaused()){
            MapThread.tileStorage.saveAll();
        }
    }
    @SubscribeEvent
    public static void onPlayerDeath(ScreenEvent.Opening event){
        if (!(event.getScreen() instanceof DeathScreen screen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // DO NOT be fooled by IntelliJ telling you this is always false.
        // When you join a server while being in a dead state, you don't have the causeOfDeath shown.
        // noinspection ConstantConditions
        if (screen.causeOfDeath == null) return;
        OpenmapApi.addWaypoint(new Waypoint(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                screen.causeOfDeath.getString(),
                ResourceLocation.withDefaultNamespace("textures/map/decorations/red_x.png").toString(),
                UUID.randomUUID()
                ));
    }
}

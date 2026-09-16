package dev.kubabin.openmap;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.*;
import dev.kubabin.openmap.api.markers.IconMarker;
import dev.kubabin.openmap.api.MenuItem;
import dev.kubabin.openmap.api.markers.PlayerMarker;
import dev.kubabin.openmap.layers.SimpleLayerProvider;
import dev.kubabin.openmap.waypoints.CreateWaypointScreen;
import dev.kubabin.openmap.widgets.TileWidget;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;

import static dev.kubabin.openmap.CachedTile.getSessionIdentifier;
import static dev.kubabin.openmap.Openmap.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientModEvents {
    private static int tickCounter = 0;
    private static final int TICKS_PER_UPDATE = 10; // Update every second (20 ticks)
    public static boolean cache_enabled = true;
    public static int lastCenterX = 0;
    public static int lastCenterZ = 0;
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
            Files.createDirectories(CachedTile.TILE_DIR);
        } catch (Exception e) {
            cache_enabled = false;
            Openmap.LOGGER.warn("Couldn't create openmap_tiles_cache directory: {}", e.getMessage());
        }
        // Built-in markers
        SimpleLayerProvider playerLayer = new SimpleLayerProvider();
        playerLayer.updateDataCallback = () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            playerLayer.markers.clear();
            mc.level.players().forEach(abstractClientPlayer -> {
                if (abstractClientPlayer.isInvisibleTo(mc.player)) return;
                IconMarker marker = new PlayerMarker(abstractClientPlayer.getSkin());
                marker.x = abstractClientPlayer.position().x;
                marker.y = abstractClientPlayer.position().z;
                marker.rotation = -180 + abstractClientPlayer.getYRot();
                marker.tooltip = Tooltip.create(abstractClientPlayer.getName());
                playerLayer.markers.add(marker);
            });
        };
        OpenmapApi.addLayer(Openmap.LAYER_PLAYERS, playerLayer);

        SimpleLayerProvider waypointLayer = new SimpleLayerProvider();
        OpenmapApi.addLayer(Openmap.LAYER_WAYPOINTS, waypointLayer);

        // Built-in Global Menu Items
        OpenmapApi.globalMenu.put("teleport", new MenuItem(
                ResourceLocation.withDefaultNamespace("textures/item/ender_pearl.png"),
                "Teleport",
                (mouseX, mouseY) -> {
                    Minecraft mc = Minecraft.getInstance();
                    double worldX = TileWidget.screenToWorldX(mouseX);
                    double worldZ = TileWidget.screenToWorldZ(mouseY);
                    LocalPlayer player = mc.player;
                    if (player == null) return;
                    player.connection.sendCommand("tp "+worldX+" 100 "+worldZ);

                    if (mc.screen == null) return;
                    mc.screen.onClose();
                    mc.setScreen(null);
                }
        ));
        OpenmapApi.globalMenu.put("add-waypoint", new MenuItem(
                ResourceLocation.fromNamespaceAndPath(MODID,"textures/gui/menu_icons/plus.png"),
                "Add waypoint",
                ((mouseX, mouseY) -> {
                    ParentScreen screen = (ParentScreen) Minecraft.getInstance().screen;
                    if (screen == null) return;
                    screen.openScreen(new CreateWaypointScreen((int) mouseX, (int) mouseY, screen));
                })
        ));
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
    public static void onPlayerTick(PlayerTickEvent.Post event){
        if (!event.getEntity().isLocalPlayer()) return;
        BlockPos playerPos = event.getEntity().blockPosition();
        boolean crossedBlockBoundary = playerPos.getX() != lastCenterX || playerPos.getZ() != lastCenterZ;
        tickCounter++;
        if (crossedBlockBoundary || tickCounter >= TICKS_PER_UPDATE){
            lastCenterX = playerPos.getX();
            lastCenterZ = playerPos.getZ();
            MinimapThreadManager.process(event.getEntity(), event.getEntity().level());
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event){
        if (!event.getLevel().isClientSide()) return;
        CachedTile.ensureLevelDir((ClientLevel) event.getLevel());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){
        while (toggleMapKey.get().consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                MinimapThreadManager.pause = true;
                mc.setScreen(new WorldmapScreen());
            } else if (mc.screen instanceof WorldmapScreen) {
                MinimapThreadManager.pause = false;
                mc.setScreen(null);
            }
        }
    }

    @SubscribeEvent
    public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event){
        CachedTile.worldName = CachedTile.getSafeFolderName(getSessionIdentifier());
        CachedTile.ensureDir();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event){
        MinimapThreadManager.tileStorage.cleanup();
    }

    @SubscribeEvent
    public static void onClientPause(ClientPauseChangeEvent.Post event){
        if (event.isPaused()){
            MinimapThreadManager.tileStorage.saveAll();
        }
    }
}

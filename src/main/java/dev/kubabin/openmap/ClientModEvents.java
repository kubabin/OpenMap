package dev.kubabin.openmap;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.kubabin.openmap.api.IconMarker;
import dev.kubabin.openmap.api.Marker;
import dev.kubabin.openmap.api.MenuItem;
import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.widgets.TileWidget;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;

import static dev.kubabin.openmap.Openmap.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientModEvents {
    private static int tickCounter = 0;
    private static final int TICKS_PER_UPDATE = 20; // Update every second (20 ticks)
    public static final int MAP_SIZE = 128; // On-screen size in pixels; blocks visible per side
    public static final int CAPTURE_SIZE = MAP_SIZE + 2; // 1-block margin on each side for smooth sub-pixel scrolling
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
        Marker playerMarker = new IconMarker(
                ResourceLocation.withDefaultNamespace("textures/map/decorations/player.png")
        );

        playerMarker.width = 16;
        playerMarker.height = 16;
        playerMarker.menuItems.put("test", new MenuItem(
                ResourceLocation.withDefaultNamespace("textures/item/ender_pearl.png"),
                "Test",
                (mouseX, mouseY) -> {}
        ));
        OpenmapApi.addMarker("player", playerMarker);
        // Built-in Global Menu Items
        OpenmapApi.globalMenu.put("teleport", new MenuItem(
                ResourceLocation.withDefaultNamespace("textures/item/ender_pearl.png"),
                "Teleport",
                (mouseX, mouseY) -> {
                    double worldX = TileWidget.screenToWorldX(mouseX);
                    double worldZ = TileWidget.screenToWorldZ(mouseY);
                    //Minecraft.getInstance().player.teleportTo(worldX, 100, worldZ);
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player == null) return;
                    player.connection.sendCommand("tp "+worldX+" 100 "+worldZ);

                    Openmap.LOGGER.info("Teleporting to {}, 100, {}",worldX, worldZ);
                    if (Minecraft.getInstance().screen == null) return;
                    Minecraft.getInstance().screen.onClose();
                    Minecraft.getInstance().setScreen(null);
                }
        ));
        OpenmapApi.globalMenu.put("add-waypoint", new MenuItem(
                ResourceLocation.fromNamespaceAndPath(MODID,"textures/gui/menu_icons/plus/png"),
                "Add waypoint",
                ((mouseX, mouseY) -> {

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
                ResourceLocation.fromNamespaceAndPath(MODID, "openmap_hud"),
                (ClientModEvents::newRender)
        );

    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event){
        BlockPos playerPos = event.getEntity().blockPosition();
        boolean crossedBlockBoundary = playerPos.getX() != lastCenterX || playerPos.getZ() != lastCenterZ;
        tickCounter++;
        if (crossedBlockBoundary || tickCounter >= TICKS_PER_UPDATE){
            tickCounter = 0;
            lastCenterX = playerPos.getX();
            lastCenterZ = playerPos.getZ();
            MinimapThreadManager.processSnapshotAsync(
                    ChunkSnapshot.createSnapshot(event.getEntity().level(), playerPos, CAPTURE_SIZE)
            );

        }
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){
        while (toggleMapKey.get().consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                MinimapThreadManager.pause = true;
                mc.setScreen(new WorldmapScreen(Component.literal("World Map")));
            } else if (mc.screen instanceof WorldmapScreen) {
                MinimapThreadManager.pause = false;
                mc.setScreen(null);
            }
        }
    }
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event){
        MinimapThreadManager.tileStorage.cleanup();
    }
    private static void newRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof WorldmapScreen) return;
        if (mc.options.hideGui || mc.player == null || mc.level == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        guiGraphics.drawString(mc.font,
                "X: " + playerPos.getX() + "  Y: " + playerPos.getY() + "  Z: " + playerPos.getZ(),
                10, MAP_SIZE+10, 0xFFFFFFFF, false);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        ShaderInstance shader = MinimapShaderHandler.getMinimapShader();
        if (shader != null) {
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture(0, DynamicTextureManager.DYNAMIC_TEXTURE_LOCATION);
        }
        if (DynamicTextureManager.readyToUpload){
            DynamicTextureManager.readyToUpload = false;
            DynamicTextureManager.getTexture().upload();
        }
        // Sample the texture shifted by the player's sub-block fraction so the map slides smoothly
        float maxUv = Config.mapSize - MAP_SIZE;
        float u = Mth.clamp((float) (mc.player.getX() - lastCenterX) + (CAPTURE_SIZE - MAP_SIZE) / 2.0f, 0, maxUv);
        float v = Mth.clamp((float) (mc.player.getZ() - lastCenterZ) + (CAPTURE_SIZE - MAP_SIZE) / 2.0f, 0, maxUv);

        float yaw = mc.player.getViewYRot(deltaTracker.getGameTimeDeltaPartialTick(true));

        // Rotate the map around its center so the player's facing direction points up
        if (Config.rotateMap) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(MAP_SIZE / 2.0, MAP_SIZE / 2.0, 0);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(180.0f - yaw));
            guiGraphics.pose().translate(-MAP_SIZE / 2.0, -MAP_SIZE / 2.0, 0);
        }
        if (shader != null) {
            // Draw the quad manually: GuiGraphics.blit() forces the position_tex_color
            // shader internally, which would ignore our minimap shader entirely
            Matrix4f mapMatrix = guiGraphics.pose().last().pose();
            float u0 = u / Config.mapSize;
            float v0 = v / Config.mapSize;
            float u1 = (u + MAP_SIZE) / Config.mapSize;
            float v1 = (v + MAP_SIZE) / Config.mapSize;
            shader.safeGetUniform("Circular").set(Config.circularMap ? 1.0f : 0.0f);
            shader.safeGetUniform("MaskUvMin").set(u0, v0);
            shader.safeGetUniform("MaskUvSize").set(u1 - u0, v1 - v0);
            BufferBuilder mapBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            mapBuffer.addVertex(mapMatrix, 0, 0, 0).setUv(u0, v0).setColor(1f, 1f, 1f, 1f);
            mapBuffer.addVertex(mapMatrix, 0, MAP_SIZE, 0).setUv(u0, v1).setColor(1f, 1f, 1f, 1f);
            mapBuffer.addVertex(mapMatrix, MAP_SIZE, MAP_SIZE, 0).setUv(u1, v1).setColor(1f, 1f, 1f, 1f);
            mapBuffer.addVertex(mapMatrix, MAP_SIZE, 0, 0).setUv(u1, v0).setColor(1f, 1f, 1f, 1f);
            BufferUploader.drawWithShader(mapBuffer.buildOrThrow());
        } else {
            guiGraphics.blit(
                    DynamicTextureManager.DYNAMIC_TEXTURE_LOCATION,
                    0, 0, // XY screen pos
                    MAP_SIZE, MAP_SIZE, // Screen size
                    u, v, // Texture UV offset (sub-pixel)
                    MAP_SIZE, MAP_SIZE, // Texture region size, sampled 1:1
                    Config.mapSize, Config.mapSize // Full texture size
            );
        }
        if (Config.rotateMap) {
            guiGraphics.pose().popPose();
        }
        // Draw player marker in the center, rotated to match the player's yaw
        // (points straight up when the map itself rotates)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(MAP_SIZE / 2.0, MAP_SIZE / 2.0, 0);
        if (!Config.rotateMap) {
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(yaw + 180));
        }
        Matrix4f markerMatrix = guiGraphics.pose().last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        // Black outline
        buffer.addVertex(markerMatrix, 0, -8, 0).setColor(0f, 0f, 0f, 1f);
        buffer.addVertex(markerMatrix, -5.5f, 6.5f, 0).setColor(0f, 0f, 0f, 1f);
        buffer.addVertex(markerMatrix, 5.5f, 6.5f, 0).setColor(0f, 0f, 0f, 1f);
        // White arrow
        buffer.addVertex(markerMatrix, 0, -6, 0).setColor(1f, 1f, 1f, 1f);
        buffer.addVertex(markerMatrix, -4, 5, 0).setColor(1f, 1f, 1f, 1f);
        buffer.addVertex(markerMatrix, 4, 5, 0).setColor(1f, 1f, 1f, 1f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        guiGraphics.pose().popPose();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
    private static void oldRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        // Don't render if the F3 debug menu is shown or if the player is null or the level is null or if the GUI is hidden
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        Player player = mc.player;
        Level level = mc.level;

        // Define screen position (Top Right Corner)
        int mapSize = Config.mapSize; // Visual size on HUD in pixels
        int screenX = mc.getWindow().getGuiScaledWidth() - mapSize - 10;
        int screenY = 10;

        // Background border
        guiGraphics.fill(screenX - 1, screenY - 1, screenX + mapSize + 1, screenY + mapSize + 1, 0xFFFF0000);

        int radius = 16; // Scans a 33x33 area centered on player
        int scale = 1;   // Size of each minimap pixel on screen

        BlockPos playerPos = player.blockPosition();

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                // Find world block position
                int currentX = playerPos.getX() + xOffset;
                int currentZ = playerPos.getZ() + zOffset;

                // Find top solid block height
                int highestY = level.getHeight(Heightmap.Types.WORLD_SURFACE, currentX, currentZ);
                BlockPos targetPos = new BlockPos(currentX, highestY - 1, currentZ);

                // Fetch the map color assigned to that block state
                int color = level.getBlockState(targetPos).getMapColor(level, targetPos).col;

                // Add full opacity flag (AARRGGBB) to the color hex
                int argbColor = 0xFF000000 | color;

                // Calculate where to draw this block on the screen relative to player center
                int drawX = screenX + (mapSize / 2) + (xOffset * scale);
                int drawY = screenY + (mapSize / 2) + (zOffset * scale);

                // Clip drawing to keep it strictly inside the map bounds
                if (drawX >= screenX && drawY >= screenY) {
                    guiGraphics.fill(drawX, drawY, drawX + scale, drawY + scale, argbColor);
                }
            }
        }

        // Draw Player Marker (Red dot in the absolute center)
        int playerCenterX = screenX + (mapSize / 2);
        int playerCenterY = screenY + (mapSize / 2);
        guiGraphics.fill(playerCenterX - 1, playerCenterY - 1, playerCenterX + 1, playerCenterY + 1, 0xFFFF0000);
        guiGraphics.drawString(mc.font, "X: " + playerPos.getX() + " Z: " + playerPos.getZ(), screenX, screenY + mapSize + 5, 0xFFFFFFFF, false);

    }
    @SubscribeEvent
    public static void onClientPause(ClientPauseChangeEvent.Post event){
        if (event.isPaused()){
            MinimapThreadManager.tileStorage.saveAll();
        }
    }
}

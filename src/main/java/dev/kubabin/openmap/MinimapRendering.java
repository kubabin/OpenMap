package dev.kubabin.openmap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;

import java.io.IOException;

import static dev.kubabin.openmap.ClientModEvents.*;

@EventBusSubscriber(modid = Openmap.MODID, value = Dist.CLIENT)
public class MinimapRendering {
    private static ShaderInstance minimapShader;
    private static ShaderInstance worldmapShader;
    private static ShaderInstance topoShader;
    public static ShaderInstance getMinimapShader() {
        return minimapShader;
    }
    public static ShaderInstance getWorldmapShader() {
        return worldmapShader;
    }
    public static ShaderInstance getTopoShader() {
        return topoShader;
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "minimap_shader"),
                        DefaultVertexFormat.POSITION_TEX_COLOR
                ),
                shaderInstance -> minimapShader = shaderInstance
        );
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "worldmap"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                shaderInstance -> worldmapShader = shaderInstance
        );
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "worldmap_topo"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                shaderInstance -> topoShader = shaderInstance
        );
    }

    public static void renderMinimap(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return;

        if (mc.screen instanceof WorldmapScreen) return;
        if (mc.options.hideGui || mc.player == null || mc.level == null) return;
        if (mc.getDebugOverlay().showDebugScreen()) return;

        BlockPos playerPos = mc.player.blockPosition();
        guiGraphics.drawString(mc.font,
                "X: " + playerPos.getX() + "  Y: " + playerPos.getY() + "  Z: " + playerPos.getZ(),
                10, MAP_SIZE+10, 0xFFFFFFFF, false);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        ShaderInstance shader = MinimapRendering.getMinimapShader();
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
}

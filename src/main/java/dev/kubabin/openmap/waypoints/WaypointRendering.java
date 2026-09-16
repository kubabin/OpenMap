package dev.kubabin.openmap.waypoints;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.OpenmapApi;
import dev.kubabin.openmap.api.markers.Marker;
import dev.kubabin.openmap.layers.SimpleLayerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import static net.minecraft.client.renderer.RenderStateShard.*;

@EventBusSubscriber(modid = Openmap.MODID, value = Dist.CLIENT)
public class WaypointRendering {
    private static final double WAYPOINT_BEAM_DISTANCE_SQR = 32.0 * 32.0;
    private static final float WAYPOINT_MARKER_SCALE_PER_BLOCK = 0.004f;
    private static final float MIN_WAYPOINT_MARKER_SCALE = 0.01f;
    private static final float MAX_WAYPOINT_MARKER_SCALE = 2.0f;
    public static double distance;
    static RenderLevelStageEvent.Stage wp_stage;
    @SubscribeEvent
    public static void onRenderWaypoints(RenderLevelStageEvent event) {
        //System.out.println("Stage: " + event.getStage());
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        SimpleLayerProvider waypointLayer = (SimpleLayerProvider) OpenmapApi.getLayer(Openmap.LAYER_WAYPOINTS);
        if (!waypointLayer.isVisible()) return;

        //MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(4_096));
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        for (Marker marker : waypointLayer.markers) {
            if (!(marker instanceof WaypointMarker waypoint)) continue;

            double waypointX = waypoint.x;
            double waypointY = waypoint.getWorldY();
            double waypointZ = waypoint.y;
            double horizontalDistanceSqr = mc.player.distanceToSqr(waypointX, mc.player.getY(), waypointZ);
            float markerScale = Mth.clamp(
                    (float) Math.sqrt(mc.player.distanceToSqr(waypointX, waypointY, waypointZ))
                            * WAYPOINT_MARKER_SCALE_PER_BLOCK,
                    MIN_WAYPOINT_MARKER_SCALE,
                    MAX_WAYPOINT_MARKER_SCALE
            );
            poseStack.pushPose();
            poseStack.translate(waypointX - cameraPosition.x + 0.5,
                    waypointY - cameraPosition.y + 0.5,
                    waypointZ - cameraPosition.z + 0.5);


            float alpha = (float) Math.min(horizontalDistanceSqr / WAYPOINT_BEAM_DISTANCE_SQR, 1f);
            distance = alpha;
            renderWaypointBeam(poseStack, cameraPosition, waypointX, waypointY, waypointZ,
                    mc.level.getMaxBuildHeight(), alpha);

            renderWaypointMarker(
                    poseStack, bufferSource, event, waypoint, waypointX, waypointY, waypointZ, cameraPosition, markerScale
            );
            poseStack.popPose();
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        bufferSource.endBatch();
    }

    private static void renderWaypointBeam(
            PoseStack poseStack, Vec3 cameraPosition, double x, double y, double z, int maxBuildHeight, float alpha
    ) {
        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder vertices = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float height = Math.max(1.0f, maxBuildHeight - (float) y);
        addBeamPlane(vertices, matrix, height, false, alpha);
        addBeamPlane(vertices, matrix, height, true, alpha);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        BufferUploader.drawWithShader(vertices.buildOrThrow());
        poseStack.popPose();
    }

    private static void addBeamPlane(
            VertexConsumer vertices, Matrix4f matrix, float height, boolean alongZ, float alpha
    ) {
        if (alongZ) {
            vertices.addVertex(matrix, 0, 0, -0.5f).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0, height, -0.5f).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0, height, 0.5f).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0, 0, 0.5f).setColor(1.0f, 1.0f, 1.0f, alpha);
        } else {
            vertices.addVertex(matrix, -0.5f, 0, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, -0.5f, height, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0.5f, height, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0.5f, 0, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
        }
    }

    private static void renderWaypointMarker(
            PoseStack poseStack, MultiBufferSource bufferSource, RenderLevelStageEvent event,
            WaypointMarker waypoint, double x, double y, double z, Vec3 cameraPosition, float markerScale
    ) {
        poseStack.pushPose();

        poseStack.mulPose(event.getCamera().rotation());
        poseStack.scale(markerScale, -markerScale, markerScale);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer iconVertices = bufferSource.getBuffer(renderType(waypoint.getIcon()));
        addWaypointIconVertex(iconVertices, matrix, -8, -16, 0, 0);
        addWaypointIconVertex(iconVertices, matrix, -8, 0, 0, 1);
        addWaypointIconVertex(iconVertices, matrix, 8, 0, 1, 1);
        addWaypointIconVertex(iconVertices, matrix, 8, -16, 1, 0);

        String name = waypoint.getName();
        Minecraft mc = Minecraft.getInstance();
        mc.font.drawInBatch(
                name,
                -mc.font.width(name) / 2.0f,
                (float) 0,
                0xFFFFFFFF,
                true,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0x00000000,
                LightTexture.FULL_BRIGHT
        );
        poseStack.popPose();
    }

    private static void addWaypointIconVertex(
            VertexConsumer vertices, Matrix4f matrix, float x, float y, float u, float v
    ) {
        vertices.addVertex(matrix, x, y, 0)
                .setColor(1.0f, 1.0f, 1.0f, 1.0f)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 0, 1);
    }
    private static RenderType renderType(ResourceLocation rl){
        return RenderType.create(
                rl.toString(),
                DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexShader))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST) // This makes it render over everything
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setTextureState(new TextureStateShard(rl, false, false))
                        .setLayeringState(new LayeringStateShard("on_top",
                                WaypointRendering::enableLayering, WaypointRendering::disableLayering))
                        .createCompositeState(true)
        );
    }
    private static void enableLayering(){
        RenderSystem.enableDepthTest();
    }
    private static void disableLayering(){
        RenderSystem.disableDepthTest();
    }
}

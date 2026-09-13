package dev.kubabin.openmap.waypoints;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.OpenmapApi;
import dev.kubabin.openmap.layers.SimpleLayerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = Openmap.MODID, value = Dist.CLIENT)
public class WaypointRendering {
    private static final double WAYPOINT_BEAM_DISTANCE_SQR = 32.0 * 32.0;
    private static final float WAYPOINT_MARKER_SCALE_PER_BLOCK = 0.004f;
    private static final float MIN_WAYPOINT_MARKER_SCALE = 0.01f;
    private static final float MAX_WAYPOINT_MARKER_SCALE = 2.0f;
    @SubscribeEvent
    public static void onRenderWaypoints(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        SimpleLayerProvider waypointLayer = (SimpleLayerProvider) OpenmapApi.getLayer(Openmap.LAYER_WAYPOINTS);
        if (!waypointLayer.isVisible()) return;

        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(4_096));
        Vec3 cameraPosition = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        for (var marker : waypointLayer.markers) {
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
            if (horizontalDistanceSqr > WAYPOINT_BEAM_DISTANCE_SQR) {
                renderWaypointBeam(poseStack, cameraPosition, waypointX, waypointY, waypointZ, mc.level.getMaxBuildHeight());
            }
            renderWaypointMarker(
                    poseStack, bufferSource, event, waypoint, waypointX, waypointY, waypointZ, cameraPosition, markerScale
            );
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        bufferSource.endBatch();
    }

    private static void renderWaypointBeam(
            PoseStack poseStack, Vec3 cameraPosition, double x, double y, double z, int maxBuildHeight
    ) {
        poseStack.pushPose();
        poseStack.translate(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder vertices = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float height = Math.max(1.0f, maxBuildHeight - (float) y);
        addBeamPlane(vertices, matrix, height, false);
        addBeamPlane(vertices, matrix, height, true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(vertices.buildOrThrow());
        poseStack.popPose();
    }

    private static void addBeamPlane(
            VertexConsumer vertices, Matrix4f matrix, float height, boolean alongZ
    ) {
        float alpha = 0.35f;
        if (alongZ) {
            vertices.addVertex(matrix, 0, 0, -0.15f).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0, height, -0.15f).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0, height, 0.15f).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0, 0, 0.15f).setColor(1.0f, 1.0f, 1.0f, alpha);
        } else {
            vertices.addVertex(matrix, -0.15f, 0, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, -0.15f, height, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0.15f, height, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
            vertices.addVertex(matrix, 0.15f, 0, 0).setColor(1.0f, 1.0f, 1.0f, alpha);
        }
    }

    private static void renderWaypointMarker(
            PoseStack poseStack, MultiBufferSource bufferSource, RenderLevelStageEvent event,
            WaypointMarker waypoint, double x, double y, double z, Vec3 cameraPosition, float markerScale
    ) {
        poseStack.pushPose();
        poseStack.translate(x - cameraPosition.x + 0.5, y - cameraPosition.y, z - cameraPosition.z + 0.5);
        poseStack.mulPose(event.getCamera().rotation());
        poseStack.scale(markerScale, -markerScale, markerScale);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer iconVertices = bufferSource.getBuffer(RenderType.entityTranslucent(waypoint.getIcon()));
        addWaypointIconVertex(iconVertices, matrix, -8, -16, 0, 0);
        addWaypointIconVertex(iconVertices, matrix, -8, 0, 0, 1);
        addWaypointIconVertex(iconVertices, matrix, 8, 0, 1, 1);
        addWaypointIconVertex(iconVertices, matrix, 8, -16, 1, 0);

        String name = waypoint.getName();
        Minecraft mc = Minecraft.getInstance();
        /*mc.font.drawInBatch(
                name,
                -mc.font.width(name) / 2.0f,
                2.0f,
                0xFFFFFFFF,
                true,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0x00000000,
                LightTexture.FULL_BRIGHT
        );*/
        mc.font.drawInBatch8xOutline(
                FormattedCharSequence.forward(name, Style.EMPTY),
                -mc.font.width(name)/ 2.0f,
                0,
                0xFF_FF_FF_FF,
                0,
                matrix,
                bufferSource,
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
}

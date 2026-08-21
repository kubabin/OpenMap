package dev.kubabin.openmap.widgets;

import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.kubabin.openmap.CachedTile;
import dev.kubabin.openmap.DynamicTextureManager;
import dev.kubabin.openmap.MinimapShaderHandler;
import dev.kubabin.openmap.MinimapThreadManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class TileWidget extends AbstractWidget {
    public static int translateX = 0;
    public static int translateY = 0;
    public static double scale = 1;
    public static final double scaleScroll = 0.1;
    private final int regionX;
    private final int regionZ;
    private final int screenX;
    private final int screenY;
    private final CachedTile tile;
    public TileWidget(int screenX, int screenY, int regionX, int regionZ) {
        super(screenX, screenY, CachedTile.WIDTH, CachedTile.HEIGHT, Component.literal(""));
        this.screenX = screenX;
        this.screenY = screenY;
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.tile = MinimapThreadManager.tileStorage.openRegionFile(regionX, regionZ);

        this.setX((int) (this.screenX*scale));
        this.setY((int) (this.screenY*scale));

        this.width = (int) (CachedTile.WIDTH*scale);
        this.height = (int) (CachedTile.HEIGHT*scale);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
        DynamicTexture texture = DynamicTextureManager.getTexture();
        if (texture == null) {
            return;
        }
        synchronized (tile) {
            ByteBuffer pixelData = tile.data.duplicate();
            pixelData.clear();

            GlStateManager._bindTexture(texture.getId());
            GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_ALIGNMENT, 1);

            GL11.glTexSubImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    CachedTile.WIDTH,
                    CachedTile.HEIGHT,
                    GL11.GL_RGB,
                    GL11.GL_UNSIGNED_BYTE,
                    pixelData
            );
        }
        int width = CachedTile.WIDTH;
        int height = CachedTile.HEIGHT;
        RenderSystem.setShaderTexture(0, texture.getId());
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShader(MinimapShaderHandler::getWorldmapShader);
        makePose(guiGraphics);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float x = (float) regionX*512;
        float y = (float) regionZ*512;
        // Bottom left
        bufferBuilder.addVertex(matrix4f, x, y+height, 0).setUv(0, 1);
        // Bottom right
        bufferBuilder.addVertex(matrix4f, x+width, y+height, 0).setUv(1, 1);
        // Top right
        bufferBuilder.addVertex(matrix4f, x+width, y, 0).setUv(1, 0);
        // Top left
        bufferBuilder.addVertex(matrix4f, x, y, 0).setUv(0, 0);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        guiGraphics.pose().popPose();
        //tile.data.flip();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        translateX += (int) dragX;
        translateY += (int) dragY;
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scale += scaleScroll * scrollY;
        this.setX((int) (this.screenX*scale));
        this.setY((int) (this.screenY*scale));
        this.width = (int) (CachedTile.WIDTH * scale);
        this.height = (int) (CachedTile.HEIGHT * scale);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    public static int getTranslateX(){
        return translateX;
    }
    public static int getTranslateY(){
        return translateY;
    }
    public static double getScale(){
        return scale;
    }

    public static void makePose(GuiGraphics guiGraphics){
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(translateX, translateY,0);
        guiGraphics.pose().scale((float) scale, (float) scale, 1);

    }
    public static Pair<Double, Double> screenToWorldCoords(double x, double y){
        double worldX = (x - translateX) / scale;
        double worldZ = (y - translateY) / scale;
        return Pair.of(worldX, worldZ);
    }
    public static double screenToWorldX(double x){
        return (x - translateX) / scale;
    }
    public static double screenToWorldZ(double y){
        return (y - translateY) / scale;
    }

}

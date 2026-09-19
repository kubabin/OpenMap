package dev.kubabin.openmap;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.kubabin.openmap.layers.LayerProvider;
import dev.kubabin.openmap.widgets.IconButton;
import dev.kubabin.openmap.widgets.MenuWidget;
import dev.kubabin.openmap.widgets.TileWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Map;

public class WorldmapScreen extends ParentScreen {
    public WorldmapScreen() {
        super(Component.translatable("key.openmap.worldmap"));
    }

    private static MenuWidget menuWidget;
    private double transition_ticks = -1;
    public static final int TRANSITION_LENGTH = 20; // ticks
    private static boolean isGlobalMenu; // Is menuWidget the global menu or a marker's menu?
    private double transition_start_x;
    private double transition_start_y;
    private double transition_diff_x;
    private double transition_diff_y;

    @Override
    protected void init() {
        super.init();
        MinimapThreadManager.tileStorage.cleanup_regions = false;
        MinimapThreadManager.pause = true;
        Minecraft mc = Minecraft.getInstance();
        BlockPos playerPos = mc.player.blockPosition();

        for (LayerProvider layer : OpenmapApi.layers.values()) {
            layer.updateInitialData();
        }

        // Other widgets
        // XYZ position
        this.addRenderableOnly(
                new StringWidget(
                        Component.literal("X: " + playerPos.getX() + "  Y: " + playerPos.getY() + "  Z: " + playerPos.getZ()),
                        Minecraft.getInstance().font)
        );
        // Button for centering the map on the player
        IconButton centerPlayerBtn = new IconButton(
                ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/markers/player.png"),
                15, 20,
                this::onPlayerButtonClick
                );
        centerPlayerBtn.setTooltip(Tooltip.create(Component.translatable("key.openmap.centerplayer")));
        centerPlayerBtn.setPosition(0, 10);
        this.addRenderableWidget(centerPlayerBtn);

        // Layer toggle checkboxes
        int y = 35;
        for (Map.Entry<String, LayerProvider> entry : OpenmapApi.layers.entrySet()) {
            String key = entry.getKey();
            LayerProvider layer = entry.getValue();
            Checkbox checkbox = Checkbox.builder(Component.translatable(key), Minecraft.getInstance().font)
                    .onValueChange((checkbox1, b) -> {
                        if (b) {
                            layer.show();
                        } else {
                            layer.hide();
                        }
                    })
                    .selected(layer.isVisible())
                    .pos(0, y)
                    .build();
            this.addRenderableWidget(checkbox);
            y += 20;
        }
    }

    private void renderTile(GuiGraphics guiGraphics, int tileX, int tileY) {
        CachedTile tile = MinimapThreadManager.tileStorage.openRegionFile(tileX, tileY);
        if (tile == null) return;
        DynamicTexture texture = DynamicTextureManager.worldmapTexture;
        if (texture == null) {
            return;
        }
        /*guiGraphics.fill(
                tileX * 512, tileY * 512,
                tileX*512+512, tileY*512+512,
                0x00000000
        );
        /*guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/gui/worldmap-bg.png"),
                tileX * 512, tileY * 512,
                0,0,
                512, 512,
                512, 512
        );*/

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
        RenderSystem.setShader(MinimapRendering::getWorldmapShader);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float x = (float) tileX * 512;
        float y = (float) tileY * 512;
        // Bottom left
        bufferBuilder.addVertex(matrix4f, x, y + height, 0).setUv(0, 1);
        // Bottom right
        bufferBuilder.addVertex(matrix4f, x + width, y + height, 0).setUv(1, 1);
        // Top right
        bufferBuilder.addVertex(matrix4f, x + width, y, 0).setUv(1, 0);
        // Top left
        bufferBuilder.addVertex(matrix4f, x, y, 0).setUv(0, 0);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
    /*private void renderTopo(GuiGraphics guiGraphics, int tileX, int tileY){
        DynamicTexture texture = DynamicTextureManager.getTexture();
        if (texture == null) {
            return;
        }
        CachedTile tile = MinimapThreadManager.tileStorage.openRegionFile(tileX, tileY);
        synchronized (tile) {
            ByteBuffer pixelData = tile.topoData.duplicate();
            pixelData.clear();

            GlStateManager._bindTexture(texture.getId());
            GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GlStateManager._pixelStore(GL11.GL_UNPACK_ALIGNMENT, 1);

            GL30.glTexSubImage2D(
                    GL30.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    CachedTile.WIDTH,
                    CachedTile.HEIGHT,
                    GL30.GL_RED_INTEGER,
                    GL30.GL_SHORT,
                    pixelData
            );
        }
        int width = CachedTile.WIDTH;
        int height = CachedTile.HEIGHT;
        RenderSystem.setShaderTexture(0, texture.getId());
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShader(MinimapShaderHandler::getTopoShader);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float x = (float) tileX*512;
        float y = (float) tileY*512;
        // Bottom left
        bufferBuilder.addVertex(matrix4f, x, y+height, 0).setUv(0, 1);
        // Bottom right
        bufferBuilder.addVertex(matrix4f, x+width, y+height, 0).setUv(1, 1);
        // Top right
        bufferBuilder.addVertex(matrix4f, x+width, y, 0).setUv(1, 0);
        // Top left
        bufferBuilder.addVertex(matrix4f, x, y, 0).setUv(0, 0);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }*/

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        TileWidget.makePose(guiGraphics);
        double tileW = CachedTile.WIDTH * TileWidget.scale;
        double tileH = CachedTile.HEIGHT * TileWidget.scale;
        int startX = (int) Math.floor((-TileWidget.translateX) / tileW);
        int startY = (int) Math.floor((-TileWidget.translateY) / tileH);
        int endX = (int) Math.ceil((guiGraphics.guiWidth() - TileWidget.translateX) / tileW);
        int endY = (int) Math.ceil((guiGraphics.guiHeight() - TileWidget.translateY) / tileH);
        for (int tileY = startY; tileY < endY; tileY++) {
            for (int tileX = startX; tileX < endX; tileX++) {
                renderTile(guiGraphics, tileX, tileY);
            }
        }

        for (LayerProvider layer : OpenmapApi.layers.values()) {
            layer.render(guiGraphics, mouseX, mouseY);
        }

        guiGraphics.pose().popPose();
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (menuWidget != null) {
            menuWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        super.renderChild(guiGraphics, mouseX, mouseY, partialTicks);
        if (transition_ticks >= 0) {
            double progress = transition_ticks / TRANSITION_LENGTH;

            // Screwing around with different easing functions
            //progress = getBezierEasing(progress, 0.42, 0.0, 0.58, 1.0);
            //progress = getBezierEasing(progress, 0.65, 0, 0.35, 1); // 'cubic'
            //progress = getBezierEasing(progress, 0.87, 0, 0.13, 1); // 'expo'
            progress = getBezierEasing(progress, 0.68, -0.6, 0.32, 1.6); // 'back'; this one is so goofy
            TileWidget.translateX = transition_start_x + (transition_diff_x * progress);
            TileWidget.translateY = transition_start_y + (transition_diff_y * progress);
            transition_ticks += 0.1d;
            if (transition_ticks > TRANSITION_LENGTH) {
                transition_ticks = -1;
            }
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        TileWidget.scale += TileWidget.scaleScroll * scrollY;
        this.width = (int) (CachedTile.WIDTH * TileWidget.scale);
        this.height = (int) (CachedTile.HEIGHT * TileWidget.scale);
        return true;

    }

    // 0 = Left Click
    // 1 = Right Click
    // 2 = Middle (scroll wheel) click
    // 3 = Back
    // 4 = Forward
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.childClicked(mouseX, mouseY, button)) return true;
        // Check if something in the menu has been clicked
        if (menuWidget != null && menuWidget.visible) {
            if (menuWidget.mouseClicked(mouseX, mouseY, button)) {
                this.closeMenu();
                return true;
            }
        }
        for (String key : OpenmapApi.layers.keySet()) {
            LayerProvider layer = OpenmapApi.getLayer(key);
            if (layer.clicked((int) mouseX, (int) mouseY, button)) {
                return true;
            }
        }
        // Check if something else has been clicked
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        // If there is a left click outside the menu or anything, close the menu.
        if (menuWidget != null && menuWidget.visible) {
            this.closeMenu();
            return true;
        }

        if (button == 1) {
            if (menuWidget == null) {
                menuWidget = new MenuWidget((int) mouseX, (int) mouseY, OpenmapApi.globalMenu);
            } else {
                menuWidget.setX((int) mouseX);
                menuWidget.setY((int) mouseY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        TileWidget.translateX += dragX;
        TileWidget.translateY += dragY;
        return true;
    }

    public void showMenu(MenuWidget widget) {
        menuWidget = widget;
        isGlobalMenu = false;
    }

    public void closeMenu() {
        menuWidget = null;
    }

    @Override
    public void onClose() {
        super.onClose();
        menuWidget = null;
        NativeImage image = new NativeImage(Config.getMapSize(), Config.getMapSize(), false);
        DynamicTextureManager.replaceImageAndUpload(image);
        MinimapThreadManager.pause = false;
        MinimapThreadManager.tileStorage.cleanup_regions = true;
        MinimapThreadManager.updateMap();
    }

    @Override
    public void tick() {
        super.tick();

        for (LayerProvider layer : OpenmapApi.layers.values()) {
            layer.updateData();
        }
    }

    public void startTransition(double targetWorldX, double targetWorldZ) {
        targetWorldX = this.width / 2.0 - targetWorldX * TileWidget.getScale();
        targetWorldZ = this.height / 2.0 - targetWorldZ * TileWidget.getScale();
        transition_ticks = 0;
        transition_start_x = TileWidget.getTranslateX();
        transition_start_y = TileWidget.getTranslateY();
        transition_diff_x = targetWorldX - transition_start_x;
        transition_diff_y = targetWorldZ - transition_start_y;
    }

    private void onPlayerButtonClick(Button button) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        startTransition(mc.player.getX(), mc.player.getZ());
    }

    public static double getBezierEasing(double t, double x1, double y1, double x2, double y2) {
        if (t <= 0.0) return 0.0;
        if (t >= 1.0) return 1.0;

        // Newton-Raphson iteration to solve for the curve parameter 'u' given time 't'
        double u = t;
        for (int i = 0; i < 8; i++) {
            // Sample X coordinate at current u
            double currentX = 3.0 * (1.0 - u) * (1.0 - u) * u * x1 + 3.0 * (1.0 - u) * u * u * x2 + u * u * u;

            // Sample X derivative (slope) at current u
            double slope = 3.0 * (1.0 - u) * (1.0 - u) * x1 + 6.0 * (1.0 - u) * u * (x2 - x1) + 3.0 * u * u * (1.0 - x2);

            if (Math.abs(slope) < 1e-6) break;
            u -= (currentX - t) / slope;
        }

        // Return the Y coordinate (the actual visual progress) at the solved parameter 'u'
        return 3.0 * (1.0 - u) * (1.0 - u) * u * y1 + 3.0 * (1.0 - u) * u * u * y2 + u * u * u;
    }
}

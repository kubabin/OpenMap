package dev.kubabin.openmap;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.layers.LayerProvider;
import dev.kubabin.openmap.sidebuttons.SideButton;
import dev.kubabin.openmap.sidebuttons.ToolSideButton;
import dev.kubabin.openmap.tools.MapTool;
import dev.kubabin.openmap.widgets.MenuWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class WorldmapScreen extends ParentScreen {

    public static final double scaleScroll = 0.1;
    private static MenuWidget menuWidget;
    public static MapTool activeTool;
    private double transition_ticks = -1;
    private static boolean isGlobalMenu; // Is menuWidget the global menu or a marker's menu?
    private double transition_start_x;
    private double transition_start_y;
    private double transition_diff_x;
    private double transition_diff_y;
    public static double translateX = 0;
    public static double translateY = 0;
    public static double scale = 1;
    public static Level currentLevel;

    public WorldmapScreen() {
        super(Component.translatable("key.openmap.worldmap"));
    }

    @Override
    protected void init() {
        super.init();
        MapThread.tileStorage.cleanup_regions = false;
        MapThread.updateMinimap = false;
        Minecraft mc = Minecraft.getInstance();
        BlockPos playerPos = mc.player.blockPosition();

        currentLevel = mc.level;
        for (LayerProvider layer : OpenmapApi.layers) {
            layer.updateInitialData();
        }


        int x = 1;

        // Layer toggle checkboxes
        int y = 10;
        for (LayerProvider layer : OpenmapApi.layers) {
            Component key = layer.getName();
            Checkbox checkbox = Checkbox.builder(key, Minecraft.getInstance().font)
                    .onValueChange((checkbox1, b) -> {
                        if (b) {
                            layer.show();
                        } else {
                            layer.hide();
                        }
                    })
                    .selected(layer.isVisible())
                    .pos(x, y)
                    .build();
            this.addRenderableWidget(checkbox);
            y += 20;
        }

        for (SideButton button : OpenmapApi.topSideButtons.values()) {
            button.setX(x);
            button.setY(y);
            this.addRenderableWidget(button);
            y += button.getHeight() + 5;
        }
        y = (height / 2) - (OpenmapApi.mapTools.size() / 2 * 20);
        for (MapTool tool : OpenmapApi.mapTools.values()) {
            ToolSideButton button = new ToolSideButton(tool);
            button.setX(x);
            button.setY(y);
            this.addRenderableWidget(button);
            y += button.getHeight() + 5;
        }
        y = height - 16;
        for (SideButton button : OpenmapApi.bottomSideButtons.values()) {
            button.setX(x);
            button.setY(y);
            this.addRenderableWidget(button);
            y -= button.getHeight() + 5;
        }

        // Center the map on the player
        translateX = (width / 2.0) - (playerPos.getX() * scale);
        translateY = (height / 2.0) - (playerPos.getZ() * scale);
    }

    private void renderTile(GuiGraphics guiGraphics, int tileX, int tileY) {
        CachedTile tile = MapThread.tileStorage.tryRegionFile(tileX, tileY);
        if (tile == null) return;
        DynamicTexture texture = DynamicTextureManager.worldmapTexture;
        if (texture == null) {
            return;
        }
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
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
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
        if (Config.renderChunkBorders)
        {
            guiGraphics.blit(
                    ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/gui/worldmap-fg.png"),
                    tileX * 512, tileY * 512,
                    0, 0,
                    512, 512,
                    512, 512
            );
        }

        RenderSystem.disableBlend();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBlurredBackground(partialTicks);
        makePose(guiGraphics);
        double tileW = CachedTile.WIDTH * scale;
        double tileH = CachedTile.HEIGHT * scale;
        int startX = (int) Math.floor((-translateX) / tileW);
        int startY = (int) Math.floor((-translateY) / tileH);
        int endX = (int) Math.ceil((guiGraphics.guiWidth() - translateX) / tileW);
        int endY = (int) Math.ceil((guiGraphics.guiHeight() - translateY) / tileH);
        for (int tileY = startY; tileY < endY; tileY++) {
            for (int tileX = startX; tileX < endX; tileX++) {
                renderTile(guiGraphics, tileX, tileY);
            }
        }

        List<Component> tooltip = new ArrayList<>();
        for (LayerProvider layer : OpenmapApi.layers) {
            layer.render(guiGraphics, mouseX, mouseY, tooltip);
        }

        guiGraphics.pose().popPose();
        guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);

        double cursorWorldX = screenToWorldX(mouseX);
        double cursorWorldZ = screenToWorldZ(mouseY);
        guiGraphics.drawString(Minecraft.getInstance().font,
                "World: X: " + (int) (cursorWorldX) + " Z: " + (int) (cursorWorldZ),
                25, guiGraphics.guiHeight() - 15, 0xFFFFFFFF);
        // player pos
        guiGraphics.drawString(Minecraft.getInstance().font,
                "Player: X: " + Minecraft.getInstance().player.getBlockX() +
                        " Y: " + Minecraft.getInstance().player.getBlockY() +
                        " Z: " + Minecraft.getInstance().player.getBlockZ(),
                25, guiGraphics.guiHeight() - 30, 0xFFFFFFFF);
        renderSidebar(guiGraphics, mouseX, mouseY, partialTicks);
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        if (menuWidget != null) {
            menuWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        super.renderChild(guiGraphics, mouseX, mouseY, partialTicks);
        if (transition_ticks >= 0) {
            double progress = transition_ticks / ((double) Minecraft.getInstance().getFps() / 4);

            // Screwing around with different easing functions
            progress = getBezierEasing(progress, 0.42, 0.0, 0.58, 1.0);
            //progress = getBezierEasing(progress, 0.65, 0, 0.35, 1); // 'cubic'
            //progress = getBezierEasing(progress, 0.87, 0, 0.13, 1); // 'expo'
            //progress = getBezierEasing(progress, 0.68, -0.6, 0.32, 1.6); // 'back'; this one is so goofy
            translateX = transition_start_x + (transition_diff_x * progress);
            translateY = transition_start_y + (transition_diff_y * progress);
            transition_ticks += 0.1d;
            if (transition_ticks > (double) Minecraft.getInstance().getFps() / 4) {
                transition_ticks = -1;
            }
        }
    }

    private void renderSidebar(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render the sidebar background
        guiGraphics.fill(0, 0, 20, height, 0xFF5380a3);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        double cursorWorldX = screenToWorldX(mouseX);
        double cursorWorldZ = screenToWorldZ(mouseY);
        scale += scaleScroll * scrollY;
        scale = Math.max(Config.maximumZoomout, scale);
        translateX = mouseX - cursorWorldX * scale;
        translateY = mouseY - cursorWorldZ * scale;
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
        // Check if something else has been clicked
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        if (activeTool != null && activeTool.mouseClicked(mouseX, mouseY, button)) return true;

        for (LayerProvider layer : OpenmapApi.layers) {
            if (layer.clicked((int) mouseX, (int) mouseY, button)) {
                return true;
            }
        }

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
        if (activeTool != null && activeTool.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        translateX += dragX;
        translateY += dragY;
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
        for (LayerProvider layer : OpenmapApi.layers) {
            layer.onMapClose();
        }
        menuWidget = null;
        NativeImage image = new NativeImage(Config.getMapSize(), Config.getMapSize(), false);
        DynamicTextureManager.replaceImageAndUpload(image);
        MapThread.updateMinimap = true;
        MapThread.tileStorage.cleanup_regions = true;
    }

    @Override
    public void tick() {
        super.tick();

        for (LayerProvider layer : OpenmapApi.layers) {
            layer.updateData();
        }
    }

    public void startTransition(double targetWorldX, double targetWorldZ) {
        targetWorldX = this.width / 2.0 - targetWorldX * scale;
        targetWorldZ = this.height / 2.0 - targetWorldZ * scale;
        transition_ticks = 0;
        transition_start_x = translateX;
        transition_start_y = translateY;
        transition_diff_x = targetWorldX - transition_start_x;
        transition_diff_y = targetWorldZ - transition_start_y;
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

    public static void makePose(GuiGraphics guiGraphics){
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(translateX, translateY,0);
        guiGraphics.pose().scale((float) scale, (float) scale, 1);

    }

    public static double screenToWorldX(double x){
        return (x - translateX) / scale;
    }

    public static double screenToWorldZ(double y){
        return (y - translateY) / scale;
    }
}

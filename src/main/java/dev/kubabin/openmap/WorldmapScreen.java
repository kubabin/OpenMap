package dev.kubabin.openmap;

import com.mojang.blaze3d.platform.NativeImage;
import dev.kubabin.openmap.api.IconMarker;
import dev.kubabin.openmap.api.Marker;
import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.widgets.MarkerWidget;
import dev.kubabin.openmap.widgets.MenuWidget;
import dev.kubabin.openmap.widgets.TileWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WorldmapScreen extends ParentScreen {
    public WorldmapScreen(Component title) {
        super(title);
    }
    private static MenuWidget menuWidget;
    private static boolean isGlobalMenu; // Is menuWidget the global menu or a marker's menu?
    @Override
    protected void init() {
        super.init();
        MinimapThreadManager.tileStorage.cleanup_regions = false;
        MinimapThreadManager.pause = true;
        Minecraft mc = Minecraft.getInstance();
        BlockPos playerPos = mc.player.blockPosition();

        // Tiles
        for (int relX = -2; relX < 3; relX++){
            for (int relZ = -2; relZ < 3; relZ++){
                //Openmap.LOGGER.info("Adding tile {}, {}", relX, relZ);
                int absX = playerPos.getX() + (relX * CachedTile.WIDTH);
                int absZ = playerPos.getZ() + (relZ * CachedTile.HEIGHT);

                this.addRenderableWidget(
                        new TileWidget(absX, absZ, relX%512, relZ%512)
                );
            }
        }
        // Markers
        IconMarker playerMarker = (IconMarker) OpenmapApi.getMarker("player");
        playerMarker.x = playerPos.getX();
        playerMarker.y = playerPos.getZ();
        playerMarker.rotation = -mc.player.getViewYRot(0);

        for (Marker marker : OpenmapApi.getMarkers().values()){
            this.addRenderableWidget(
                    new MarkerWidget(marker)
            );
        }

        // Other widgets
        this.addRenderableOnly(
                new StringWidget(
                        Component.literal("X: " + playerPos.getX() + "  Y: " + playerPos.getY() + "  Z: " + playerPos.getZ()),
                        Minecraft.getInstance().font)
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {
        super.render(guiGraphics, i, i1, v);
        TileWidget.makePose(guiGraphics);
        for (String key : OpenmapApi.getMarkers().keySet()){
            Marker marker = OpenmapApi.getMarkers().get(key);
            marker.render(guiGraphics);
        }
        guiGraphics.pose().popPose();
    }

    public void closeMenu(){
        this.removeWidget(menuWidget);
        menuWidget = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)){
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
        // Check if something in the menu has been clicked
        if (menuWidget != null && menuWidget.visible) {
            if (menuWidget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        // Check if something else has been clicked
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        // If there is a left click outside the menu or anything, close the menu.
        if (menuWidget != null && menuWidget.visible){
            this.removeWidget(menuWidget);
            menuWidget = null;
            return true;
        }

        if (button == 1) {
            if (menuWidget == null) {
                menuWidget = new MenuWidget((int) mouseX, (int) mouseY, OpenmapApi.globalMenu);
                this.addRenderableWidget(menuWidget);
            } else {
                menuWidget.setX((int) mouseX);
                menuWidget.setY((int) mouseY);
            }
            return true;
        }

        return true;
    }
    @Override
    public boolean mouseDragged(double p_94699_, double p_94700_, int p_94701_, double p_94702_, double p_94703_) {
        return super.mouseDragged(p_94699_, p_94700_, p_94701_, p_94702_, p_94703_);
    }
    public void showMenu(MenuWidget widget){
        menuWidget = widget;
        isGlobalMenu = false;
    }



    @Override
    public void onClose() {
        super.onClose();
        menuWidget = null;
        NativeImage image = new NativeImage(Config.mapSize, Config.mapSize, false);
        DynamicTextureManager.replaceImageAndUpload(image);
        MinimapThreadManager.pause = false;
        MinimapThreadManager.tileStorage.cleanup_regions = true;
        MinimapThreadManager.updateMap();
    }
}

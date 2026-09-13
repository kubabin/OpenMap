package dev.kubabin.openmap.layers;

import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.api.markers.Marker;
import dev.kubabin.openmap.widgets.MenuWidget;
import dev.kubabin.openmap.widgets.TileWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;

public class SimpleLayerProvider implements LayerProvider {
    private boolean visible = true;
    public ArrayList<Marker> markers = new ArrayList<>();
    public Runnable updateDataCallback;
    public Runnable updateInitialDataCallback;
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!visible) return;
        double worldX = TileWidget.screenToWorldX(mouseX);
        double worldZ = TileWidget.screenToWorldZ(mouseY);
        for (Marker marker : markers){
            marker.render(guiGraphics);
            int left = (int) (marker.x - marker.width / 2);
            int right = (int) (marker.x + marker.width / 2);
            int top = (int) (marker.y - marker.height / 2);
            int bottom = (int) (marker.y + marker.height/2);
            if (worldX > left && worldX < right &&
                    worldZ > top && worldZ < bottom){
                if (marker.tooltip != null){
                    guiGraphics.pose().popPose();
                    guiGraphics.renderTooltip(Minecraft.getInstance().font,
                            marker.tooltip.toCharSequence(Minecraft.getInstance()),
                            mouseX, mouseY);
                    TileWidget.makePose(guiGraphics);
                }
            }
        }
    }

    @Override
    public boolean clicked(int x, int y, int button) {
        if (button != 1) return false;
        double mouseX = TileWidget.screenToWorldX(x);
        double mouseY = TileWidget.screenToWorldZ(y);
        for (Marker marker : markers){
            double left = marker.x - marker.width / 2d;
            double right = marker.x + marker.width / 2d;
            double top = marker.y - marker.height / 2d;
            double bottom = marker.y + marker.height / 2d;

            if (mouseX >= left && mouseX <= right &&
                    mouseY >= top && mouseY <= bottom) {
                WorldmapScreen worldmap = (WorldmapScreen) Minecraft.getInstance().screen;
                if (worldmap != null) {
                    worldmap.showMenu(new MenuWidget(x, y, marker.menuItems));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void updateInitialData() {
        if (updateInitialDataCallback != null){
            updateInitialDataCallback.run();
        }
    }

    @Override
    public void updateData() {
        if (updateDataCallback != null){
            updateDataCallback.run();
        }
    }


    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void hide() {
        visible = false;
    }

    @Override
    public void show() {
        visible = true;
    }
}

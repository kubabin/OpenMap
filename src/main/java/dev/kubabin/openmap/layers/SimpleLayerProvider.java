package dev.kubabin.openmap.layers;

import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.api.markers.Marker;
import dev.kubabin.openmap.widgets.MenuWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SimpleLayerProvider extends LayerProvider {
    public ArrayList<Marker> markers = new ArrayList<>();
    public Component name;
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip) {
        if (!isVisible()) return;
        double worldX = WorldmapScreen.screenToWorldX(mouseX);
        double worldZ = WorldmapScreen.screenToWorldZ(mouseY);
        for (Marker marker : markers){
            marker.render(guiGraphics);
            int left = (int) (marker.x - marker.width / 2);
            int right = (int) (marker.x + marker.width / 2);
            int top = (int) (marker.y - marker.height / 2);
            int bottom = (int) (marker.y + marker.height/2);
            if (worldX > left && worldX < right &&
                    worldZ > top && worldZ < bottom){
                if (marker.tooltip != null){
                    tooltip.add(marker.tooltip);
                }
            }
        }
    }

    @Override
    public boolean clicked(int x, int y, int button) {
        if (button != 1) return false;
        double mouseX = WorldmapScreen.screenToWorldX(x);
        double mouseY = WorldmapScreen.screenToWorldZ(y);
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
    public void updateInitialData() {}

    @Override
    public void updateData() {}

    @Override
    public Component getName() {
        return name != null ? name : Component.literal("Unnamed Layer");
    }

    @Override
    public void onMapClose() {}

}

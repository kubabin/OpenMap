package dev.kubabin.openmap.layers;

import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.datasource.MarkerSource;
import dev.kubabin.openmap.widgets.MenuWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamedLayer extends LayerProvider {
    MarkerSource source;
    public StreamedLayer(MarkerSource source){
        this.source = source;
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip) {
        if (!isVisible()) return;
        final int finalMouseX = (int) WorldmapScreen.screenToWorldX(mouseX);
        final int finalMouseY = (int) WorldmapScreen.screenToWorldZ(mouseY);
        source.getMarkers().forEach(marker -> {
            marker.render(guiGraphics);
            int left = (int) (marker.x - (double) marker.width / 2);
            int right = (int) (marker.x + (double) marker.width / 2);
            int top = (int) (marker.y - (double) marker.height / 2);
            int bottom = (int) (marker.y + (double) marker.height/2);
            if (finalMouseX > left && finalMouseX < right &&
                    finalMouseY > top && finalMouseY < bottom){
                if (marker.tooltip != null){
                    tooltip.add(marker.tooltip);
                }
            }
        });
    }

    @Override
    public boolean clicked(int x, int y, int button) {
        if (button != 1) return false;
        AtomicBoolean handled = new AtomicBoolean(false);
        source.getMarkers()
                .filter(marker -> marker.clicked(x,y,button))
                .forEach(marker -> {
                    WorldmapScreen worldmap = (WorldmapScreen) Minecraft.getInstance().screen;
                    if (worldmap != null) {
                        worldmap.showMenu(new MenuWidget(x, y, marker.menuItems));
                        handled.set(true);
                    }
                });
        return handled.get();
    }

    @Override
    public void updateInitialData() {

    }

    @Override
    public void updateData() {

    }

    @Override
    public Component getName() {
        return source.getName();
    }

    @Override
    public void onMapClose() {}
}

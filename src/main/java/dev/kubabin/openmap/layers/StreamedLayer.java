package dev.kubabin.openmap.layers;

import dev.kubabin.openmap.datasource.MarkerSource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class StreamedLayer extends LayerProvider {
    MarkerSource source;
    public StreamedLayer(MarkerSource source){
        this.source = source;
    }
    @Override
    public Component render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isVisible()) return null;
        MutableComponent tooltip = Component.empty();
        source.getMarkers().forEach(marker -> {
            marker.render(guiGraphics);
            int left = (int) (marker.x - marker.width / 2);
            int right = (int) (marker.x + marker.width / 2);
            int top = (int) (marker.y - marker.height / 2);
            int bottom = (int) (marker.y + marker.height/2);
            if (mouseX > left && mouseX < right &&
                    mouseY > top && mouseY < bottom){
                if (marker.tooltip != null){
                    tooltip.append(marker.tooltip);
                }
            }
        });
        return tooltip;
    }

    @Override
    public boolean clicked(int x, int y, int button) {
        return false;
    }

    @Override
    public void updateInitialData() {

    }

    @Override
    public void updateData() {

    }
}

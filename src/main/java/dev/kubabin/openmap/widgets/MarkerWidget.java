package dev.kubabin.openmap.widgets;

import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.api.Marker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class MarkerWidget extends BetterAbstractWidget {
    private final Marker marker;
    public MarkerWidget(Marker marker) {
        super(marker.x, marker.y, marker.width, marker.height, Component.empty());
        this.marker = marker;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        TileWidget.makePose(guiGraphics);
        marker.render(guiGraphics);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean onRightClick(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        WorldmapScreen screen = (WorldmapScreen) mc.screen;
        MenuWidget widget = new MenuWidget((int) mouseX, (int) mouseY, marker.menuItems);
        screen.showMenu(widget);
        return super.onRightClick(mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}

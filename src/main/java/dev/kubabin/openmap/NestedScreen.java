package dev.kubabin.openmap;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NestedScreen extends Screen {
    private final int x,y;
    public NestedScreen(int x, int y, Component title) {
        super(title);
        this.x=x;
        this.y=y;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int p_281550_, int p_282878_, float p_282465_) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x,y,0);
        super.render(guiGraphics, p_281550_, p_282878_, p_282465_);
        guiGraphics.pose().popPose();
    }
}

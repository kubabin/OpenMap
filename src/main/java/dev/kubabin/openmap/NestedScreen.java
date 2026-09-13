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
    public boolean mouseClicked(double mouseX, double mouseY, int p_94697_) {
        return super.mouseClicked(mouseX-x, mouseY-y, p_94697_);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_282465_) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x,y,0);
        super.render(guiGraphics, mouseX-x, mouseY-y, p_282465_);
        guiGraphics.pose().popPose();
    }

    @Override
    public void onClose() {

    }
}

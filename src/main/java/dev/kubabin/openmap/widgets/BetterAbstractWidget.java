package dev.kubabin.openmap.widgets;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class BetterAbstractWidget extends AbstractWidget {
    public BetterAbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public boolean onRightClick(double mouseX, double mouseY){
        return false;
    }
    public boolean onLeftClick(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;
        if (!this.clicked(mouseX, mouseY)) return false;
        if (button == 0) return onLeftClick(mouseX, mouseY);
        if (button == 1) return onRightClick(mouseX, mouseY);
        return false;
    }
}

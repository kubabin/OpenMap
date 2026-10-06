package dev.kubabin.openmap.layers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class LayerProvider {
    private boolean isVisible = true;
    /**
     * Renders the layer and returns a tooltip if the mouse is hovering over it.
     * @param guiGraphics GuiGraphics context to draw with
     * @param mouseX Mouse screen X position
     * @param mouseY Mouse screen Y position
     * @return Tooltip to display, or null if no tooltip should be displayed
     */
    public abstract Component render(GuiGraphics guiGraphics, int mouseX, int mouseY);

    /**
     * @param x Mouse screen X position
     * @param y Mouse screen Y position
     * @param button Which button was clicked. 0=left, 1=right, 2=middle, 3=back, 4=forward
     * @return Has the click event been handled?
     */
    public abstract boolean clicked(int x, int y, int button);
    // Usually called when the worldmap opens.
    public abstract void updateInitialData();
    // Called every tick
    public abstract void updateData();
    public boolean isVisible() {
        return isVisible;
    }
    public void hide() {
        isVisible = false;
    }
    public void show() {
        isVisible = true;
    }
}

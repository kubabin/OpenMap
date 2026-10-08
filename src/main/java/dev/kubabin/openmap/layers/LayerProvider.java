package dev.kubabin.openmap.layers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class LayerProvider {
    private boolean isVisible = true;
    /**
     * Renders the layer and adds tooltip components if the mouse is hovering over it.
     * @param guiGraphics GuiGraphics context to draw with
     * @param mouseX Mouse screen X position
     * @param mouseY Mouse screen Y position
     * @param tooltip List of tooltip components to add to
     */
    public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip);

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
    public abstract Component getName();
    // Called right before closing the worldmap.
    public abstract void onMapClose();
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

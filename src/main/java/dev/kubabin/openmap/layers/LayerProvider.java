package dev.kubabin.openmap.layers;

import net.minecraft.client.gui.GuiGraphics;

public interface LayerProvider {
    void render(GuiGraphics guiGraphics, int mouseX, int mouseY);

    /**
     * @param x Mouse screen X position
     * @param y Mouse screen Y position
     * @param button Which button was clicked. 0=left, 1=right, 2=middle, 3=back, 4=forward
     * @return Has the click event been handled?
     */
    boolean clicked(int x, int y, int button);
    // Usually called when the worldmap opens.
    void updateInitialData();
    // Called every tick
    void updateData();
    boolean isVisible();
    void hide();
    void show();
}

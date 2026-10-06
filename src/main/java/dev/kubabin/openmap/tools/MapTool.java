package dev.kubabin.openmap.tools;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class MapTool {
    /**
     * Called when the mouse is clicked.
     * @param mouseX Screen X coordinate of the mouse click.
     * @param mouseY Screen Y coordinate of the mouse click.
     * @param button The mouse button that was clicked.
     * @return True if the click was handled, false otherwise.
     */
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    /**
     * Called when the mouse is dragged.
     * @param mouseX Screen X coordinate of the mouse drag.
     * @param mouseY Screen Y coordinate of the mouse drag.
     * @param button The mouse button that is being held down.
     * @param deltaX The change in X coordinate since the last drag event.
     * @param deltaY The change in Y coordinate since the last drag event.
     * @return True if the drag was handled, false otherwise.
     */
    public abstract boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
    public abstract ResourceLocation getIcon();
    public abstract Component getTooltip();
}

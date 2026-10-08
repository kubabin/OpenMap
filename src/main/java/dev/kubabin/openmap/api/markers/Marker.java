package dev.kubabin.openmap.api.markers;

import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.api.MenuItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.HashMap;

public abstract class Marker {
    // X and Y world coords.
    public double x = 0;
    public double y = 0;
    // Width and height in pixels, used for clicks.
    public int width = 0;
    public int height = 0;
    public Component tooltip;
    public abstract void render(GuiGraphics guiGraphics);
    public final HashMap<String, MenuItem> menuItems = new HashMap<>();
    public boolean clicked(int mouseX, int mouseY, int button){
        mouseX = (int) WorldmapScreen.screenToWorldX(mouseX);
        mouseY = (int) WorldmapScreen.screenToWorldZ(mouseY);
        double left = x - width / 2d;
        double right = x + width / 2d;
        double top = y - height / 2d;
        double bottom = y + height / 2d;

        return mouseX >= left && mouseX <= right &&
                mouseY >= top && mouseY <= bottom;
    }
}

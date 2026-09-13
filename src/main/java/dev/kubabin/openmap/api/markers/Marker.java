package dev.kubabin.openmap.api.markers;

import dev.kubabin.openmap.api.MenuItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;

import java.util.HashMap;

public abstract class Marker {
    // X and Y world coords.
    public double x = 0;
    public double y = 0;
    // Width and height in pixels, used for clicks.
    public int width = 0;
    public int height = 0;
    public int id = 0;
    public Tooltip tooltip;
    public abstract void render(GuiGraphics guiGraphics);
    public final HashMap<String, MenuItem> menuItems = new HashMap<>();
}

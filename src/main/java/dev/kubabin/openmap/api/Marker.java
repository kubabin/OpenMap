package dev.kubabin.openmap.api;

import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;

public abstract class Marker {

    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;
    public abstract void render(GuiGraphics guiGraphics);
    public final HashMap<String, MenuItem> menuItems = new HashMap<>();


}

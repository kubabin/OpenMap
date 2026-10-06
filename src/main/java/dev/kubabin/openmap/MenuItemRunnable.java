package dev.kubabin.openmap;

public interface MenuItemRunnable {
    /**
     *
     * @param mouseX Screen X coordinate where the menu was opened. NOT the current mouse coordinate.
     * @param mouseY Screen Y coordinate where the menu was opened. NOT the current mouse coordinate.
     */
    void run(double mouseX, double mouseY);
    /**
     * @param blockX The X coordinate of the block that was clicked.
     * @param blockZ The Z coordinate of the block that was clicked.
     * @return Whether this menu item should be displayed in the menu for the given block coordinates.
     */
    boolean displayInMenu(double blockX, double blockZ);
}

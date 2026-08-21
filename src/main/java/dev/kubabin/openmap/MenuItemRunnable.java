package dev.kubabin.openmap;

public interface MenuItemRunnable {
    /**
     *
     * @param mouseX X coordinate where the menu was opened. NOT the current mouse coordinate.
     * @param mouseY Y coordinate where the menu was opened. NOT the current mouse coordinate.
     */
    void run(double mouseX, double mouseY);
}

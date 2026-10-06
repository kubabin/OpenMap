package dev.kubabin.openmap.menuitems;

import dev.kubabin.openmap.MenuItemRunnable;
import dev.kubabin.openmap.ParentScreen;
import dev.kubabin.openmap.api.MenuItem;
import dev.kubabin.openmap.waypoints.CreateWaypointScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static dev.kubabin.openmap.Openmap.MODID;

public class CreateWaypointMenuItem implements MenuItemRunnable {
    @Override
    public void run(double mouseX, double mouseY) {
        ParentScreen screen = (ParentScreen) Minecraft.getInstance().screen;
        if (screen == null) return;
        screen.openScreen(new CreateWaypointScreen((int) mouseX, (int) mouseY, screen));
    }

    @Override
    public boolean displayInMenu(double blockX, double blockZ) {
        return true;
    }

    public static MenuItem createMenuItem() {
        return new MenuItem(
                ResourceLocation.fromNamespaceAndPath(MODID,"textures/gui/menu_icons/plus.png"),
                Component.translatable("key.openmap.waypoint.create"),
                new CreateWaypointMenuItem()
        );
    }
}

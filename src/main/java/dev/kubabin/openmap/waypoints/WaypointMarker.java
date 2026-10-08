package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.menuitems.MenuItemRunnable;
import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.api.markers.IconMarker;
import dev.kubabin.openmap.api.MenuItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WaypointMarker extends IconMarker {
    protected String name;
    public WaypointMarker(Waypoint wp) {
        super(ResourceLocation.parse(wp.icon()));
        this.x = (int) wp.x();
        this.y = (int) wp.z();
        this.name = wp.name();
        this.width = 8;
        this.height = 8;
        this.tooltip = Component.literal(wp.name());

        if (Minecraft.getInstance().player.getPermissionLevel() >= 2) {
            this.menuItems.put("teleport", new MenuItem(
                    ResourceLocation.withDefaultNamespace("textures/item/ender_pearl.png"),
                    Component.translatable("key.openmap.teleport"),
                    new MenuItemRunnable() {
                        @Override
                        public void run(double mouseX, double mouseY) {
                            Minecraft.getInstance().player.connection.sendCommand(
                                    "tp "+wp.x()+" "+ wp.y() +" "+wp.z()
                            );
                            if (Minecraft.getInstance().screen == null) return;
                            Minecraft.getInstance().screen.onClose();
                            Minecraft.getInstance().setScreen(null);

                        }

                        @Override
                        public boolean displayInMenu(double blockX, double blockZ) {
                            return Minecraft.getInstance().player.getPermissionLevel() >= 2;
                        }
                    }
            ));
        }

        this.menuItems.put("delete", new MenuItem(
                ResourceLocation.fromNamespaceAndPath(Openmap.MODID,
                        "textures/gui/menu_icons/delete.png"),
                Component.translatable("key.openmap.delete"),
                new MenuItemRunnable() {
                    @Override
                    public void run(double mouseX, double mouseY) {
                        WaypointClientStorage.deleteWaypoint(wp.uuid());
                    }

                    @Override
                    public boolean displayInMenu(double blockX, double blockZ) {
                        return true;
                    }
                }
        ));
    }

    public String getName() {
        return name;
    }
}

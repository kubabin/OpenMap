package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.OpenmapApi;
import dev.kubabin.openmap.api.markers.IconMarker;
import dev.kubabin.openmap.api.MenuItem;
import dev.kubabin.openmap.layers.SimpleLayerProvider;
import dev.kubabin.openmap.waypoints.networking.DeleteWaypointPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class WaypointMarker extends IconMarker {
    protected String name;
    private final UUID uuid;
    private double worldY;
    public WaypointMarker(Waypoint wp) {
        super(ResourceLocation.parse(wp.icon()));
        this.x = (int) wp.x();
        this.worldY = wp.y();
        this.y = (int) wp.z();
        this.name = wp.name();
        this.uuid = wp.uuid();
        this.width = 8;
        this.height = 8;

        if (Minecraft.getInstance().player.getPermissionLevel() >= 2) {
            this.menuItems.put("teleport", new MenuItem(
                    ResourceLocation.withDefaultNamespace("textures/item/ender_pearl.png"),
                    Component.translatable("key.openmap.teleport"),
                    (x,y) -> {
                        Minecraft.getInstance().player.connection.sendCommand(
                                "tp "+wp.x()+" "+ wp.y() +" "+wp.z()
                        );
                        if (Minecraft.getInstance().screen == null) return;
                        Minecraft.getInstance().screen.onClose();
                        Minecraft.getInstance().setScreen(null);
                    }
            ));
        }

        this.menuItems.put("delete", new MenuItem(
                ResourceLocation.fromNamespaceAndPath(Openmap.MODID,
                        "textures/gui/menu_icons/delete.png"),
                Component.translatable("key.openmap.delete"),
                (x,y) -> {
                    SimpleLayerProvider wpLayer = (SimpleLayerProvider) OpenmapApi.getLayer(Openmap.LAYER_WAYPOINTS);
                    PacketDistributor.sendToServer(
                            new DeleteWaypointPayload(this.uuid)
                    );
                    wpLayer.markers.remove(this);
                }
        ));
    }

    public double getWorldY() {
        return worldY;
    }

    public String getName() {
        return name;
    }
}

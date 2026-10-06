package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.NestedScreen;
import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.ParentScreen;
import dev.kubabin.openmap.WorldmapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class CreateWaypointScreen extends NestedScreen {
    private final double waypointX;
    private final double waypointZ;
    private EditBox nameEditBox;
    private final ParentScreen parent;
    public CreateWaypointScreen(int x, int y, ParentScreen parent) {
        super(x, y, Component.translatable("key.openmap.waypoint.create"));
        this.waypointX = WorldmapScreen.screenToWorldX(x);
        this.waypointZ = WorldmapScreen.screenToWorldZ(y);
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        if (minecraft == null){
            this.minecraft = Minecraft.getInstance();
        }
        nameEditBox = new EditBox(minecraft.font, 80, 20,
                Component.translatable("key.openmap.waypoint.name")
        );
        nameEditBox.setMaxLength(128);
        this.addRenderableWidget(nameEditBox);
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> this.onDone()
        ).pos(0, 20).width(40).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> this.onCancel()
        ).pos(40, 20).width(40).build());
    }
    private void onDone(){
        String name = nameEditBox.getValue().toLowerCase();
        ResourceLocation icon = ResourceLocation.withDefaultNamespace("textures/map/decorations/white_banner.png");
        if (name.contains("portal")){
            if (name.contains("nether")){
                icon = ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/markers/nether_portal.png");
            } else if (name.contains("end") || name.contains("ender")){
                icon = ResourceLocation.withDefaultNamespace("textures/item/ender_eye.png");
            }
        } else if (name.contains("base")){
            icon = ResourceLocation.withDefaultNamespace("textures/map/decorations/plains_village.png");
        }
        WaypointClientStorage.addWaypoint(
                new Waypoint(
                        this.waypointX,
                        Double.NEGATIVE_INFINITY,
                        this.waypointZ,
                        nameEditBox.getValue(),
                        icon.toString(),
                        UUID.randomUUID()
                )
        );
        parent.closeScreen();
    }
    private void onCancel(){

    }
}

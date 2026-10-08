package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.NestedScreen;
import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.ParentScreen;
import dev.kubabin.openmap.WorldmapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;

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
        Minecraft mc = Minecraft.getInstance();
        double y;
        if (WaypointClientStorage.isClientSide){
            if (mc.level != null && mc.level.isLoaded(new BlockPos((int) waypointX, 0, (int) waypointZ))){
                y = mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) waypointX, (int) waypointZ);
            } else {
                y = 64;
            }
        } else {
            // Let the server figure it out
            y = Double.NEGATIVE_INFINITY;
        }
        WaypointClientStorage.addWaypoint(
                new Waypoint(
                        this.waypointX,
                        y,
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

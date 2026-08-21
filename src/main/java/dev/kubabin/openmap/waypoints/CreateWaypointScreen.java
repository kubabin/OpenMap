package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.NestedScreen;
import dev.kubabin.openmap.ParentScreen;
import dev.kubabin.openmap.widgets.TileWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class CreateWaypointScreen extends NestedScreen {
    private final double waypointX;
    private final double waypointZ;
    private EditBox nameEditBox;
    private final ParentScreen parent;
    public CreateWaypointScreen(int x, int y, ParentScreen parent) {
        super(x, y, Component.literal("Create Waypoint"));
        this.waypointX = TileWidget.screenToWorldX(x);
        this.waypointZ = TileWidget.screenToWorldZ(y);
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        nameEditBox = new EditBox(minecraft.font, 50, 10,
                Component.literal("Waypoint name...")
        );
        this.addRenderableWidget(nameEditBox);
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                button -> this.onDone()
        ).build());
        this.addRenderableWidget(Button.builder(
                Component.literal("Cancel"),
                button -> this.onCancel()
        ).build());
    }
    private void onDone(){
        PacketDistributor.sendToServer(
                new CreateWaypointPayload(
                        this.waypointX,
                        Double.NEGATIVE_INFINITY,
                        this.waypointZ,
                        nameEditBox.getValue(),
                        ResourceLocation.withDefaultNamespace("textures/map/decorations/white_banner.png")
                                .toString()
                )
        );
        parent.closeScreen();
    }
    private void onCancel(){

    }
}

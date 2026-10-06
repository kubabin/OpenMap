package dev.kubabin.openmap.sidebuttons;

import dev.kubabin.openmap.WorldmapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static dev.kubabin.openmap.Openmap.MODID;

public class CenterPlayerButton extends SideButton {
    public CenterPlayerButton() {
        super(
                ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/player.png"),
                Component.literal("key.openmap.center_player")
        );
    }

    @Override
    public void onClick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        WorldmapScreen screen = (WorldmapScreen) mc.screen;
        if (screen == null) return;
        screen.startTransition(mc.player.getX(), mc.player.getZ());
    }
}

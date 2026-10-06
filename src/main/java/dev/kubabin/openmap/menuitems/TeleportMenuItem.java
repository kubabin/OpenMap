package dev.kubabin.openmap.menuitems;

import dev.kubabin.openmap.MenuItemRunnable;
import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.api.MenuItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TeleportMenuItem implements MenuItemRunnable {
    @Override
    public void run(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        double worldX = WorldmapScreen.screenToWorldX(mouseX);
        double worldZ = WorldmapScreen.screenToWorldZ(mouseY);
        LocalPlayer player = mc.player;
        if (player == null) return;
        player.connection.sendCommand("tp "+worldX+" 100 "+worldZ);

        if (mc.screen == null) return;
        mc.screen.onClose();
        mc.setScreen(null);
    }

    @Override
    public boolean displayInMenu(double blockX, double blockZ) {
        return Minecraft.getInstance().player.hasPermissions(2);
    }
    public static MenuItem createMenuItem() {
        return new MenuItem(
                ResourceLocation.withDefaultNamespace("textures/item/ender_pearl.png"),
                Component.translatable("key.openmap.teleport"),
                new TeleportMenuItem()
        );
    }
}

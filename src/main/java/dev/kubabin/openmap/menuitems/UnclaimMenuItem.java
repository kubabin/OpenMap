package dev.kubabin.openmap.menuitems;

import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.api.MenuItem;
import net.minecraft.client.Minecraft;
import xaero.pac.client.api.OpenPACClientAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class UnclaimMenuItem implements MenuItemRunnable {
    @Override
    public void run(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        double worldX = WorldmapScreen.screenToWorldX(mouseX);
        double worldZ = WorldmapScreen.screenToWorldZ(mouseY);

        OpenPACClientAPI.get().getClaimsManager().requestUnclaim(
                mc.level.dimension().location(),
                Math.floorDiv((int) worldX, 16), Math.floorDiv((int) worldZ, 16),
                null
        );
    }

    @Override
    public boolean displayInMenu(double blockX, double blockZ) {
        return OpenPACClientAPI.get().getClaimsManager().get(
                Minecraft.getInstance().level.dimension().location(),
                Math.floorDiv((int) blockX, 16), Math.floorDiv((int) blockZ, 16)
        ) != null;
    }
    public static MenuItem createMenuItem() {
        return new MenuItem(
                ResourceLocation.withDefaultNamespace("textures/item/iron_shovel.png"),
                Component.translatable("key.openmap.unclaim"),
                new UnclaimMenuItem()
        );
    }
}

package dev.kubabin.openmap.sidebuttons;

import dev.kubabin.openmap.Openmap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MulticlaimButton extends SideButton{
    public MulticlaimButton() {
        super(
                ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/gui/multiclaim_button.png"),
                Component.literal("key.openmap.claim_multiple")
        );
    }

    @Override
    public void onClick() {

    }
}

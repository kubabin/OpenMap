package dev.kubabin.openmap.api.markers;

import dev.kubabin.openmap.Openmap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class PlayerMarker extends IconMarker{
    private PlayerSkin skin;
    public PlayerMarker(PlayerSkin skin) {
        super(ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/markers/player.png"));
        this.width = 5;
        this.height = 7;
        this.skin = skin;
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        super.render(guiGraphics);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(
                x - ((int) x), y - ((int) y), 0
        );
        guiGraphics.blit(
                skin.texture(),
                (int) (this.x+ 1), (int) (this.y+ 1),
                4,4, // scaled w h
                8, 8,
                8, 8,
                64, 64
                );
        guiGraphics.pose().popPose();
    }
}

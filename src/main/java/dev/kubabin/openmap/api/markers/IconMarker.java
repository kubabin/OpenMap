package dev.kubabin.openmap.api.markers;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class IconMarker extends Marker {
    private final ResourceLocation icon;
    public double rotation;
    // If the icon should be centered on the world position.
    public boolean drawCentered = true;
    public IconMarker(ResourceLocation icon){
        this.icon = icon;
        // TODO: Make this not hardcoded
        this.width = 16;
        this.height = 16;
    }
    @Override
    public void render(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(
                this.x + this.width / 2.0,
                this.y + this.height / 2.0,
                0
        );
        guiGraphics.pose().mulPose(
                Axis.ZP.rotationDegrees((float) this.rotation)
        );
        guiGraphics.pose().translate(
                -this.width / 2.0,
                -this.height / 2.0,
                0
        );
        guiGraphics.blit(icon,
                drawCentered ? -this.width/2 : 0,
                drawCentered ? -this.height/2 : 0,
                0, 0,
                this.width, this.height,
                this.width, this.height);
        guiGraphics.pose().popPose();
    }
    public ResourceLocation getIcon(){
        return icon;
    }
}

package dev.kubabin.openmap.api.markers;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class IconMarker extends Marker {
    private final ResourceLocation icon;
    public double rotation;
    // If the icon should be centered on the world position.
    public boolean drawCentered = true;
    public double uvOffsetX = 0;
    public double uvOffsetY = 0;
    public double scale = 1.0;
    public IconMarker(ResourceLocation icon){
        this.icon = icon;
        // TODO: Make this not hardcoded
        this.width = 16;
        this.height = 16;
    }
    @Override
    public void render(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        double width = this.width * this.scale;
        double height = this.height * this.scale;
        guiGraphics.pose().translate(
                this.x + width / 2.0,
                this.y + height / 2.0,
                0
        );
        guiGraphics.pose().mulPose(
                Axis.ZP.rotationDegrees((float) this.rotation)
        );
        guiGraphics.pose().translate(
                -width / 2.0,
                -height / 2.0,
                0
        );
        guiGraphics.pose().scale((float) this.scale, (float) this.scale, 1.0f);
        guiGraphics.blit(icon,
                drawCentered ? -this.width/2 : 0,
                drawCentered ? -this.height/2 : 0,
                (float) this.uvOffsetX, (float) this.uvOffsetY,
                this.width, this.height,
                this.width, this.height);
        guiGraphics.pose().popPose();
    }
    public ResourceLocation getIcon(){
        return icon;
    }
}

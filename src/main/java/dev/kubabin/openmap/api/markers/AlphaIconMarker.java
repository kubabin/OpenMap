package dev.kubabin.openmap.api.markers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

// Basically an IconMarker, but with controllable alpha.
public class AlphaIconMarker extends IconMarker {
    private double alpha = 1.0;
    public AlphaIconMarker(ResourceLocation icon) {
        super(icon);
    }

    @Override
    public void render(GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float) alpha);
        super.render(guiGraphics);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
    public void setAlpha(double alpha) {
        this.alpha = Math.clamp(alpha, 0.0, 1.0);
    }
}

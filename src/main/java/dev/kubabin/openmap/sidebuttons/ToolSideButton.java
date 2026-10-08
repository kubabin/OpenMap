package dev.kubabin.openmap.sidebuttons;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.WorldmapScreen;
import dev.kubabin.openmap.tools.MapTool;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ToolSideButton extends SideButton {
    private final MapTool tool;

    public ToolSideButton(MapTool tool) {
        super(tool.getIcon(), tool.getTooltip());
        this.tool = tool;
    }

    @Override
    public void onClick() {
        if (WorldmapScreen.activeTool == tool) {
            WorldmapScreen.activeTool = null;
        } else {
            WorldmapScreen.activeTool = tool;
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (WorldmapScreen.activeTool == tool) {
            guiGraphics.blit(
                    ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/gui/selected.png"),
                    getX(), getY(),
                    0, 0,
                    16, 16,
                    16, 16);
        }
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
    }
}

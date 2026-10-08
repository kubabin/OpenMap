package dev.kubabin.openmap.sidebuttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class SideButton extends AbstractButton {
    private final ResourceLocation icon;
    private final Component tooltip;

    public SideButton(ResourceLocation icon, Component tooltip) {
        super(0, 0, 16, 16, tooltip);
        this.icon = icon;
        this.tooltip = tooltip;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(getIcon(),
                getX(), getY(),
                0, 0,
                width, height,
                width, height);
        if (isHoveredOrFocused()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }

    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        //narrationElementOutput.add(NarratedElementType.TITLE, tooltip);
    }

    public abstract void onClick();

    @Override
    public final void onPress() {
        onClick();
    }
}

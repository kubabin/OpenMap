package dev.kubabin.openmap.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class IconButton extends Button {

	private final ResourceLocation texture;

	public IconButton(ResourceLocation texture, int width, int height) {
		this(texture, width, height, button -> {
		});
	}

	public IconButton(ResourceLocation texture, int width, int height, OnPress onPress) {
		this(0, 0, texture, width, height, onPress);
	}

	public IconButton(int x, int y, ResourceLocation texture, int width, int height, OnPress onPress) {
		super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.texture = texture;
	}

	@Override
	protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

		int iconX = getX() + 2;
		int iconY = getY() + 2;
		int iconWidth = Math.max(0, width - 4);
		int iconHeight = Math.max(0, height - 4);

		if (iconWidth > 0 && iconHeight > 0) {
			guiGraphics.blit(texture, iconX, iconY, 0, 0, iconWidth, iconHeight, iconWidth, iconHeight);
		}
	}

}

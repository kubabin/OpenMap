package dev.kubabin.openmap.api;

import dev.kubabin.openmap.MenuItemRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The menu is a list, shown when something is right-clicked.
 */
public record MenuItem (ResourceLocation icon, Component text, MenuItemRunnable onClick) {}

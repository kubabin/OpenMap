package dev.kubabin.openmap.api;

import dev.kubabin.openmap.MenuItemRunnable;
import net.minecraft.resources.ResourceLocation;

/**
 * The menu is a list, shown when something is right-clicked.
 */
public record MenuItem (ResourceLocation icon, String text, MenuItemRunnable onClick) {}

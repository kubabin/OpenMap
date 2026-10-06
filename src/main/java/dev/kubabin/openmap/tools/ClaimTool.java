package dev.kubabin.openmap.tools;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.WorldmapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.client.api.OpenPACClientAPI;
import xaero.pac.client.claims.api.IClientClaimsManagerAPI;

public class ClaimTool extends MapTool{
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            claimOrUnclaim(mouseX, mouseY, true);
        } else if (button == 1) {
            claimOrUnclaim(mouseX, mouseY, false);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            claimOrUnclaim(mouseX, mouseY, true);
        } else if (button == 1) {
            claimOrUnclaim(mouseX, mouseY, false);
        }
        return true;
    }
    private static void claimOrUnclaim(double screenX, double screenZ, boolean claim) {
        // if claimed, unclaim, else claim
        IClientClaimsManagerAPI claimsManager = OpenPACClientAPI.get().getClaimsManager();
        screenX = WorldmapScreen.screenToWorldX(screenX);
        screenZ = WorldmapScreen.screenToWorldZ(screenZ);
        int worldX = Math.floorDiv((int) screenX, 16);
        int worldZ = Math.floorDiv((int) screenZ, 16);
        ResourceLocation dimension = Minecraft.getInstance().level.dimension().location();
        if (claim) {
            claimsManager.requestClaim(dimension, worldX, worldZ, null);
        } else {
            claimsManager.requestUnclaim(dimension, worldX, worldZ, null);
        }
    }

    @Override
    public ResourceLocation getIcon() {
        return ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/gui/player.png");
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("key.openmap.tool.claim");
    }
}

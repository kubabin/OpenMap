package dev.kubabin.openmap.layers;

import dev.kubabin.openmap.WorldmapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import xaero.pac.client.api.OpenPACClientAPI;
import xaero.pac.client.claims.api.IClientClaimsManagerAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class OpacLayer extends LayerProvider {
    public OpacLayer() {
        super();
    }

    @Override
    public void updateInitialData() {}

    @Override
    public void updateData() {}

    @Override
    public Component render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isVisible()) return null;

        double chunkW = 16 * WorldmapScreen.scale;
        double chunkH = 16 * WorldmapScreen.scale;
        int startX = (int) Math.floor((-WorldmapScreen.translateX) / chunkW);
        int startY = (int) Math.floor((-WorldmapScreen.translateY) / chunkH);
        int endX = (int) Math.ceil((guiGraphics.guiWidth() - WorldmapScreen.translateX) / chunkW);
        int endY = (int) Math.ceil((guiGraphics.guiHeight() - WorldmapScreen.translateY) / chunkH);
        IClientClaimsManagerAPI claimsManager = OpenPACClientAPI.get().getClaimsManager();
        ResourceLocation dimension = Minecraft.getInstance().level.dimension().location();

        for (int chunkY = startY; chunkY < endY; chunkY++) {
            for (int chunkX = startX; chunkX < endX; chunkX++) {
                IPlayerChunkClaimAPI claim = claimsManager.get(
                        dimension,
                        chunkX, chunkY);
                if (claim == null) continue;
                int color = claimsManager.getPlayerInfo(claim.getPlayerId()).getClaimsColor();
                guiGraphics.fill(
                        chunkX * 16,
                        chunkY * 16,
                        (chunkX + 1) * 16,
                        (chunkY + 1) * 16,
                        0x80000000 | color // semi-transparent
                );
            }
        }
        IPlayerChunkClaimAPI mouseClaim = claimsManager.get(dimension,
                Math.floorDiv((int) WorldmapScreen.screenToWorldX(mouseX), 16),
                Math.floorDiv((int) WorldmapScreen.screenToWorldZ(mouseY), 16)
        );
        if (mouseClaim != null) {
            UUID playerId = mouseClaim.getPlayerId();
            String playerName = claimsManager.getPlayerInfo(playerId).getPlayerUsername();
            int color = claimsManager.getPlayerInfo(playerId).getClaimsColor();
            return Component.literal(playerName).withColor(color);
        }
        return null;

    }

    @Override
    public boolean clicked(int x, int y, int button) {
        return false;
    }
}

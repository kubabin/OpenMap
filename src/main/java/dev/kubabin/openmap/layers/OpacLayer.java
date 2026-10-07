package dev.kubabin.openmap.layers;

import dev.kubabin.openmap.WorldmapScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xaero.pac.client.api.OpenPACClientAPI;
import xaero.pac.client.claims.api.IClientClaimsManagerAPI;
import xaero.pac.client.claims.tracker.result.api.IClaimsManagerClaimResultListenerAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OpacLayer extends LayerProvider implements IClaimsManagerClaimResultListenerAPI, IClaimsManagerListenerAPI {
    private ResourceLocation dimension;
    private IClientClaimsManagerAPI claimsManager;
    private final List<ClaimRectangle> cachedRectangles = new ArrayList<>();
    private final Map<Long, PlayerDisplay> cachedClaims = new HashMap<>();
    private boolean cacheDirty = true;
    private int cachedStartX;
    private int cachedStartZ;
    private int cachedEndX;
    private int cachedEndZ;

    public OpacLayer() {
        super();
    }

    @Override
    public void updateInitialData() {
        dimension = WorldmapScreen.currentLevel.dimension().location();
        claimsManager = OpenPACClientAPI.get().getClaimsManager();
        invalidateCache();
    }

    @Override
    public void updateData() {
        ResourceLocation currentDimension = WorldmapScreen.currentLevel.dimension().location();
        if (!currentDimension.equals(dimension)) {
            dimension = currentDimension;
            invalidateCache();
        }
    }

    @Override
    public Component getName() {
        return Component.translatable("key.openmap.claim");
    }

    @Override
    public void onMapClose() {
        cachedRectangles.clear();
        cachedClaims.clear();
        cacheDirty = true;
    }

    /** Called by the OPAC claim tracker after its local claim data changes. */
    public void invalidateCache() {
        cacheDirty = true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> tooltip) {
        if (!isVisible() || claimsManager == null) return;

        double chunkW = 16 * WorldmapScreen.scale;
        double chunkH = 16 * WorldmapScreen.scale;
        int startX = (int) Math.floor((-WorldmapScreen.translateX) / chunkW);
        int startY = (int) Math.floor((-WorldmapScreen.translateY) / chunkH);
        int endX = (int) Math.ceil((guiGraphics.guiWidth() - WorldmapScreen.translateX) / chunkW);
        int endY = (int) Math.ceil((guiGraphics.guiHeight() - WorldmapScreen.translateY) / chunkH);

        if (cacheDirty || startX != cachedStartX || startY != cachedStartZ || endX != cachedEndX || endY != cachedEndZ) {
            rebuildCache(startX, startY, endX, endY);
        }

        for (ClaimRectangle rectangle : cachedRectangles) {
            guiGraphics.fill(
                    rectangle.startChunkX * 16,
                    rectangle.startChunkZ * 16,
                    rectangle.endChunkX * 16,
                    rectangle.endChunkZ * 16,
                    0x80000000 | rectangle.color
            );
        }

        int mouseChunkX = Math.floorDiv((int) WorldmapScreen.screenToWorldX(mouseX), 16);
        int mouseChunkZ = Math.floorDiv((int) WorldmapScreen.screenToWorldZ(mouseY), 16);
        PlayerDisplay mouseClaim = cachedClaims.get(chunkKey(mouseChunkX, mouseChunkZ));
        if (mouseClaim != null) {
            tooltip.add(Component.literal(mouseClaim.name).withStyle(style -> style.withColor(mouseClaim.color)));
        }
    }

    private void rebuildCache(int startX, int startZ, int endX, int endZ) {
        cachedRectangles.clear();
        cachedClaims.clear();
        Map<UUID, PlayerDisplay> players = new HashMap<>();
        Map<RunKey, ClaimRectangle> previousRow = new HashMap<>();

        for (int chunkZ = startZ; chunkZ < endZ; chunkZ++) {
            Map<RunKey, ClaimRectangle> currentRow = new HashMap<>();
            int chunkX = startX;
            while (chunkX < endX) {
                IPlayerChunkClaimAPI claim = claimsManager.get(dimension, chunkX, chunkZ);
                if (claim == null) {
                    chunkX++;
                    continue;
                }

                PlayerDisplay display = getPlayerDisplay(players, claim);
                int runStartX = chunkX;
                do {
                    cachedClaims.put(chunkKey(chunkX, chunkZ), display);
                    chunkX++;
                    if (chunkX >= endX) break;
                    claim = claimsManager.get(dimension, chunkX, chunkZ);
                    if (claim == null) break;
                    PlayerDisplay nextDisplay = getPlayerDisplay(players, claim);
                    if (nextDisplay.color != display.color) break;
                    display = nextDisplay;
                } while (true);

                RunKey key = new RunKey(runStartX, chunkX, display.color);
                ClaimRectangle rectangle = previousRow.get(key);
                if (rectangle == null || rectangle.endChunkZ != chunkZ) {
                    rectangle = new ClaimRectangle(runStartX, chunkZ, chunkX, chunkZ + 1, display.color);
                    cachedRectangles.add(rectangle);
                } else {
                    rectangle.endChunkZ++;
                }
                currentRow.put(key, rectangle);
            }
            previousRow = currentRow;
        }

        cachedStartX = startX;
        cachedStartZ = startZ;
        cachedEndX = endX;
        cachedEndZ = endZ;
        cacheDirty = false;
    }

    private PlayerDisplay getPlayerDisplay(Map<UUID, PlayerDisplay> players, IPlayerChunkClaimAPI claim) {
        return players.computeIfAbsent(claim.getPlayerId(), playerId -> {
            var playerInfo = claimsManager.getPlayerInfo(playerId);
            return new PlayerDisplay(playerInfo.getPlayerUsername(), playerInfo.getClaimsColor());
        });
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
    }

    private record PlayerDisplay(String name, int color) {}

    private record RunKey(int startChunkX, int endChunkX, int color) {}

    private static final class ClaimRectangle {
        private final int startChunkX;
        private final int startChunkZ;
        private final int endChunkX;
        private int endChunkZ;
        private final int color;

        private ClaimRectangle(int startChunkX, int startChunkZ, int endChunkX, int endChunkZ, int color) {
            this.startChunkX = startChunkX;
            this.startChunkZ = startChunkZ;
            this.endChunkX = endChunkX;
            this.endChunkZ = endChunkZ;
            this.color = color;
        }
    }

    @Override
    public boolean clicked(int x, int y, int button) {
        return false;
    }
    @Override
    public void onClaimResult(@NotNull AreaClaimResult result) {
        invalidateCache();
    }

    @Override
    public void onWholeRegionChange(@NotNull ResourceLocation dimension, int regionX, int regionZ) {
        invalidateCache();
    }

    @Override
    public void onChunkChange(@NotNull ResourceLocation dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI claim) {
        invalidateCache();
    }

    @Override
    public void onDimensionChange(ResourceLocation dimension) {
        invalidateCache();
    }
}

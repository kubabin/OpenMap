package dev.kubabin.openmap.compat;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.layers.OpacLayer;
import dev.kubabin.openmap.menuitems.ClaimMenuItem;
import dev.kubabin.openmap.menuitems.UnclaimMenuItem;
import dev.kubabin.openmap.tools.ClaimTool;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import xaero.pac.client.claims.tracker.result.api.IClaimsManagerClaimResultListenerAPI;
import xaero.pac.client.event.api.OPACClientAddonRegisterEvent;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;

// Compatibility with Open Parties and Claims.
@EventBusSubscriber(modid = Openmap.MODID, value = Dist.CLIENT)
public class OpacCompat implements IClaimsManagerClaimResultListenerAPI, IClaimsManagerListenerAPI {
    private static final String OPAC_MODID = "openpartiesandclaims";
    private static OpacLayer opacLayer;

    public static void init() {
        // Check if OPAC is installed
        if (!ModList.get().isLoaded(OPAC_MODID)) {
            return;
        }
        opacLayer = new OpacLayer();
        OpenmapApi.addLayer(opacLayer);
        OpenmapApi.globalMenu.put("claim", ClaimMenuItem.createMenuItem());
        OpenmapApi.globalMenu.put("unclaim", UnclaimMenuItem.createMenuItem());
        OpenmapApi.mapTools.put("claim", new ClaimTool());
    }

    @SubscribeEvent
    public static void onRegisterClientAddon(OPACClientAddonRegisterEvent event) {
        OpacCompat compat = new OpacCompat();
        event.getContext().getClaimsManagerTrackerAPI().register(compat);
        event.getContext().getClaimsManagerClaimResultTrackerAPI().register(compat);
    }

    @Override
    public void onClaimResult(@NotNull AreaClaimResult result) {
        invalidateClaimCache();
    }

    @Override
    public void onWholeRegionChange(@NotNull ResourceLocation dimension, int regionX, int regionZ) {
        invalidateClaimCache();
    }

    @Override
    public void onChunkChange(@NotNull ResourceLocation dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI claim) {
        invalidateClaimCache();
    }

    @Override
    public void onDimensionChange(ResourceLocation dimension) {
        invalidateClaimCache();
    }

    private void invalidateClaimCache() {
        if (opacLayer != null) {
            opacLayer.invalidateCache();
        }
    }
}

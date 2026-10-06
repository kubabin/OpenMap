package dev.kubabin.openmap.compat;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.layers.LayerProvider;
import dev.kubabin.openmap.layers.OpacLayer;
import dev.kubabin.openmap.menuitems.ClaimMenuItem;
import dev.kubabin.openmap.menuitems.UnclaimMenuItem;
import dev.kubabin.openmap.tools.ClaimTool;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import xaero.pac.client.api.OpenPACClientAPI;
import xaero.pac.client.claims.tracker.result.api.IClaimsManagerClaimResultListenerAPI;
import xaero.pac.client.event.api.OPACClientAddonRegisterEvent;
import xaero.pac.common.claims.result.api.AreaClaimResult;
import xaero.pac.common.claims.result.api.ClaimResult;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;

// Compatibility with Open Parties and Claims.
@EventBusSubscriber(modid = Openmap.MODID, value = Dist.CLIENT)
public class OpacCompat implements IClaimsManagerClaimResultListenerAPI {
    private static final String OPAC_MODID = "openpartiesandclaims";
    private static boolean isOpacInstalled = false;
    public static void init() {
        // Check if OPAC is installed
        isOpacInstalled = ModList.get().isLoaded(OPAC_MODID);
        if (!isOpacInstalled) {
            return;
        }
        LayerProvider opacLayer = new OpacLayer();
        OpenmapApi.addLayer("Claims", opacLayer);
        OpenmapApi.globalMenu.put("claim", ClaimMenuItem.createMenuItem());
        OpenmapApi.globalMenu.put("unclaim", UnclaimMenuItem.createMenuItem());
        OpenmapApi.mapTools.put("claim", new ClaimTool());
    }

    @SubscribeEvent
    public static void onRegisterClientAddon(OPACClientAddonRegisterEvent event) {
        event.getContext().getClaimsManagerClaimResultTrackerAPI().register(new OpacCompat());
    }

    @Override
    public void onClaimResult(@NotNull AreaClaimResult result) {
        for (ClaimResult.Type type : result.getResultTypesIterable()) {
            if (type.success) continue;

        }
    }
}

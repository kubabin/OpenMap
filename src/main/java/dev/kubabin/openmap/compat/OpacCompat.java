package dev.kubabin.openmap.compat;

import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.layers.OpacLayer;
import dev.kubabin.openmap.menuitems.ClaimMenuItem;
import dev.kubabin.openmap.menuitems.UnclaimMenuItem;
import dev.kubabin.openmap.tools.ClaimTool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import xaero.pac.client.event.api.OPACClientAddonRegisterEvent;

// Compatibility with Open Parties and Claims.
public class OpacCompat  {
    public static final String OPAC_MODID = "openpartiesandclaims";
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
        NeoForge.EVENT_BUS.register(OpacCompat.class);
    }

    @SubscribeEvent
    public static void onRegisterClientAddon(OPACClientAddonRegisterEvent event) {
        event.getContext().getClaimsManagerTrackerAPI().register(opacLayer);
        event.getContext().getClaimsManagerClaimResultTrackerAPI().register(opacLayer);
    }
}

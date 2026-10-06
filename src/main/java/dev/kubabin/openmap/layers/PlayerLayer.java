package dev.kubabin.openmap.layers;

import dev.kubabin.openmap.api.markers.IconMarker;
import dev.kubabin.openmap.api.markers.PlayerMarker;
import net.minecraft.client.Minecraft;

public class PlayerLayer extends SimpleLayerProvider {

    @Override
    public void updateData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        markers.clear();
        mc.level.players().forEach(abstractClientPlayer -> {
            if (mc.player != null && abstractClientPlayer.isInvisibleTo(mc.player)) return;
            IconMarker marker = new PlayerMarker(abstractClientPlayer.getSkin());
            marker.x = abstractClientPlayer.position().x;
            marker.y = abstractClientPlayer.position().z;
            marker.rotation = -180 + abstractClientPlayer.getYRot();
            marker.tooltip = abstractClientPlayer.getName();
            markers.add(marker);
        });
    }
}

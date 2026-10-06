package dev.kubabin.openmap.datasource;

import dev.kubabin.openmap.api.markers.Marker;
import dev.kubabin.openmap.api.markers.PlayerMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.stream.Stream;

public class PlayerSource implements MarkerSource {
    @Override
    public Stream<Marker> getMarkers() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level.players().stream()
                .filter(player -> mc.player == null || !player.isInvisibleTo(mc.player))
                .map(player -> {
                    PlayerMarker marker = new PlayerMarker(player.getSkin());
                    marker.x = player.position().x;
                    marker.y = player.position().z;
                    marker.rotation = -180 + player.getYRot();
                    marker.tooltip = player.getName();
                    return marker;
                });
    }

    @Override
    public Component getName() {
        return Component.translatable("key.openmap.players");
    }
}

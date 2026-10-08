package dev.kubabin.openmap.datasource;

import dev.kubabin.openmap.Config;
import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.api.markers.AlphaIconMarker;
import dev.kubabin.openmap.api.markers.Marker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EntitySource implements MarkerSource{
    @Override
    public Stream<Marker> getMarkers() {
        return StreamSupport.stream(Minecraft.getInstance().level.entitiesForRendering().spliterator(), false)
                .filter(entity -> !entity.isInvisible())
                .filter(entity -> entity instanceof Mob)
                .map(entity -> {
                    ResourceLocation icon;
                    if (entity.getType().getCategory().isFriendly()){
                        icon = ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/markers/green_marker.png");
                    } else {
                        icon = ResourceLocation.fromNamespaceAndPath(Openmap.MODID, "textures/markers/red_marker.png");
                    }
                    AlphaIconMarker marker = new AlphaIconMarker(icon);
                    marker.x = entity.position().x;
                    marker.y = entity.position().z;
                    marker.drawCentered = false;
                    marker.rotation = -180 + entity.getYRot();
                    marker.tooltip = MutableComponent.create(entity.getName().getContents()).
                            withColor(entity.getType().getCategory().isFriendly() ? 0x00FF00 : 0xFF0000);
                    marker.width = 5;
                    marker.height = 7;
                    marker.scale = 0.3;
                    double playerY = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.position().y : 0;
                    double distance = Math.abs(entity.position().y - playerY);
                    marker.setAlpha(1.0 - Math.min(distance / Config.maximumEntityDistance, 1.0));
                    return marker;
                });
    }

    @Override
    public Component getName() {
        return Component.translatable("key.openmap.entities");
    }
}

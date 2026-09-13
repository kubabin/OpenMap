package dev.kubabin.openmap.waypoints.networking;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.OpenmapApi;
import dev.kubabin.openmap.api.markers.IconMarker;
import dev.kubabin.openmap.layers.SimpleLayerProvider;
import dev.kubabin.openmap.waypoints.Waypoint;
import dev.kubabin.openmap.waypoints.WaypointMarker;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WaypointSyncPayload(List<Waypoint> waypoints)
        implements CustomPacketPayload {

    public static final Type<WaypointSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Openmap.MODID,
                    "waypoint_sync"
            ));

    public static final StreamCodec<ByteBuf, WaypointSyncPayload> STREAM_CODEC =
            Waypoint.STREAM_CODEC
                    .apply(ByteBufCodecs.list(256))
                    .map(WaypointSyncPayload::new, WaypointSyncPayload::waypoints);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            final WaypointSyncPayload payload,
            final IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            SimpleLayerProvider waypointLayer = (SimpleLayerProvider) OpenmapApi.getLayer(Openmap.LAYER_WAYPOINTS);
            waypointLayer.markers.clear();
            for (Waypoint wp : payload.waypoints()){
                IconMarker wpMarker = new WaypointMarker(wp);
                wpMarker.tooltip = Tooltip.create(Component.literal(wp.name()));
                waypointLayer.markers.add(wpMarker);
            }
        });
    }
}

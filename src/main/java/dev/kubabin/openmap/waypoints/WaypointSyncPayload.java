package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.Openmap;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
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
}

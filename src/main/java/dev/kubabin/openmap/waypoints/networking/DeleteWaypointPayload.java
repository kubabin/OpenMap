package dev.kubabin.openmap.waypoints.networking;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.waypoints.Waypoint;
import dev.kubabin.openmap.waypoints.WaypointSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record DeleteWaypointPayload(UUID uuid) implements CustomPacketPayload {
    public static final Type<DeleteWaypointPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Openmap.MODID,
                    "delete_waypoint"
            ));
    public static final StreamCodec<ByteBuf, DeleteWaypointPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    DeleteWaypointPayload::uuid,
                    DeleteWaypointPayload::new
            );
    public static void handle(
            final DeleteWaypointPayload payload,
            final IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            WaypointSavedData data =
                    WaypointSavedData.get(player.serverLevel());
            List<Waypoint> waypoints = data.getWaypoints(player.getUUID());
            for (int i = 0; i < waypoints.size(); i++){
                if (waypoints.get(i).uuid().equals(payload.uuid())){
                    waypoints.remove(i);
                    break;
                }
            }

        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

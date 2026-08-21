package dev.kubabin.openmap;

import dev.kubabin.openmap.api.OpenmapApi;
import dev.kubabin.openmap.waypoints.CreateWaypointPayload;
import dev.kubabin.openmap.waypoints.Waypoint;
import dev.kubabin.openmap.waypoints.WaypointSavedData;
import dev.kubabin.openmap.waypoints.WaypointSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@EventBusSubscriber(modid = Openmap.MODID)
public final class ModNetworking {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                WaypointSyncPayload.TYPE,
                WaypointSyncPayload.STREAM_CODEC,
                ModNetworking::handleWaypointSync
        );

        registrar.playToServer(
                CreateWaypointPayload.TYPE,
                CreateWaypointPayload.STREAM_CODEC,
                ModNetworking::handleCreateWaypoint
        );
    }

    private static void handleWaypointSync(
            final WaypointSyncPayload payload,
            final IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            OpenmapApi.waypoints.addAll(payload.waypoints());
        });
    }

    private static void handleCreateWaypoint(
            final CreateWaypointPayload payload,
            final IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            // Server-side validation here.

            WaypointSavedData data =
                    WaypointSavedData.get(player.serverLevel());

            data.addWaypoint(
                    player.getUUID(),
                    payload.waypoint()
            );

            sendWaypoints(player);
        });
    }

    private static void sendWaypoints(ServerPlayer player) {
        WaypointSavedData data =
                WaypointSavedData.get(player.serverLevel());

        List<Waypoint> waypoints =
                data.getWaypoints(player.getUUID());

        PacketDistributor.sendToPlayer(
                player,
                new WaypointSyncPayload(List.copyOf(waypoints))
        );
    }
}

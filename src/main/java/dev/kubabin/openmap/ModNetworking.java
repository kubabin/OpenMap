package dev.kubabin.openmap;

import dev.kubabin.openmap.waypoints.*;
import dev.kubabin.openmap.waypoints.networking.CreateWaypointPayload;
import dev.kubabin.openmap.waypoints.networking.DeleteWaypointPayload;
import dev.kubabin.openmap.waypoints.networking.WaypointSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@EventBusSubscriber(modid = Openmap.MODID)
public final class ModNetworking {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                .optional();

        registrar.playToClient(
                WaypointSyncPayload.TYPE,
                WaypointSyncPayload.STREAM_CODEC,
                WaypointSyncPayload::handle
        );

        registrar.playToServer(
                CreateWaypointPayload.TYPE,
                CreateWaypointPayload.STREAM_CODEC,
                CreateWaypointPayload::handle
        );

        registrar.playToServer(
                DeleteWaypointPayload.TYPE,
                DeleteWaypointPayload.STREAM_CODEC,
                DeleteWaypointPayload::handle
        );
    }

    public static void sendWaypoints(ServerPlayer player) {
        WaypointSavedData data =
                WaypointSavedData.get(player.serverLevel());

        List<Waypoint> waypoints =
                data.getWaypoints(player.getUUID());
        PacketDistributor.sendToPlayer(
                player,
                new WaypointSyncPayload(List.copyOf(waypoints))
        );
    }
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event){
        sendWaypoints((ServerPlayer) event.getEntity());
    }
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        // Send waypoints to player
        if (event.getEntity().isLocalPlayer()) return;
        sendWaypoints((ServerPlayer) event.getEntity());
    }
}

package dev.kubabin.openmap.waypoints.networking;

import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.waypoints.Waypoint;
import dev.kubabin.openmap.waypoints.WaypointSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static dev.kubabin.openmap.ModNetworking.sendWaypoints;

public record CreateWaypointPayload(
        double x,
        double y,
        double z,
        String name,
        String icon,
        UUID uuid
) implements CustomPacketPayload {

    public static final Type<CreateWaypointPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Openmap.MODID,
                    "create_waypoint"
            ));

    public static final StreamCodec<ByteBuf, CreateWaypointPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE,
                    CreateWaypointPayload::x,
                    ByteBufCodecs.DOUBLE,
                    CreateWaypointPayload::y,
                    ByteBufCodecs.DOUBLE,
                    CreateWaypointPayload::z,
                    ByteBufCodecs.stringUtf8(128),
                    CreateWaypointPayload::name,
                    ByteBufCodecs.stringUtf8(128),
                    CreateWaypointPayload::icon,
                    UUIDUtil.STREAM_CODEC,
                    CreateWaypointPayload::uuid,
                    CreateWaypointPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            final CreateWaypointPayload payload,
            final IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            WaypointSavedData data =
                    WaypointSavedData.get(player.serverLevel());
            Waypoint wp = payload.waypoint();
            if (wp.y() == Double.NEGATIVE_INFINITY){
                // Figure out the Y level based on Heightmap
                double y = player.level().getHeight(Heightmap.Types.MOTION_BLOCKING, (int) wp.x(), (int) wp.z());
                wp = new Waypoint(wp.x(), y, wp.z(), wp.name(), wp.icon(), wp.uuid());
            }

            data.addWaypoint(
                    player.getUUID(),
                    wp
            );

            sendWaypoints(player);
        });
    }
    public Waypoint waypoint(){
        return new Waypoint(x,y,z,name,icon,this.uuid);
    }
    public static CreateWaypointPayload from(Waypoint waypoint){
        return new CreateWaypointPayload(waypoint.x(), waypoint.y(), waypoint.z(), waypoint.name(), waypoint.icon(),
                waypoint.uuid());
    }
}

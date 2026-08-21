package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.Openmap;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CreateWaypointPayload(
        double x,
        double y,
        double z,
        String name,
        String icon
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
                    CreateWaypointPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public Waypoint waypoint(){
        return new Waypoint(x,y,z,name,icon);
    }
}

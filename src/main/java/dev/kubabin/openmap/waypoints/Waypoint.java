package dev.kubabin.openmap.waypoints;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record Waypoint(double x, double y, double z, String name, String icon) {
    public static final StreamCodec<ByteBuf, Waypoint> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE,
                    Waypoint::x,
                    ByteBufCodecs.DOUBLE,
                    Waypoint::y,
                    ByteBufCodecs.DOUBLE,
                    Waypoint::z,
                    ByteBufCodecs.stringUtf8(128),
                    Waypoint::name,
                    ByteBufCodecs.stringUtf8(128),
                    Waypoint::icon,
                    Waypoint::new
            );
}

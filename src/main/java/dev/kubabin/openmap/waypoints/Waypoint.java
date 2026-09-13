package dev.kubabin.openmap.waypoints;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record Waypoint(double x, double y, double z, String name, String icon, UUID uuid) {
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
                    UUIDUtil.STREAM_CODEC,
                    Waypoint::uuid,
                    Waypoint::new
            );
    public void render(GuiGraphics guiGraphics){
        guiGraphics.blit(
                ResourceLocation.withDefaultNamespace("textures/map/decorations/white_banner.png"),
                0, 0,
                (float) this.x, (float) this.y,
                16,16,16,16
        );
    }
}

package dev.kubabin.openmap.waypoints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
    public static final Codec<Waypoint> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("x").forGetter(Waypoint::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(Waypoint::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(Waypoint::z),
                    Codec.STRING.fieldOf("name").forGetter(Waypoint::name),
                    Codec.STRING.fieldOf("icon").forGetter(Waypoint::icon),
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(Waypoint::uuid)
            ).apply(instance, Waypoint::new)
    );
    public void render(GuiGraphics guiGraphics){
        guiGraphics.blit(
                ResourceLocation.withDefaultNamespace("textures/map/decorations/white_banner.png"),
                0, 0,
                (float) this.x, (float) this.y,
                16,16,16,16
        );
    }
    public CompoundTag toTag() {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this)
                .getOrThrow(RuntimeException::new); // Or handle the DataResult safely
    }
    public static Waypoint fromTag(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(RuntimeException::new);
    }
}

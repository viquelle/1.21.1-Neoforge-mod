package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DynamicBrightPayload(float bright) implements CustomPacketPayload {
    public static final Type<DynamicBrightPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "bright_sync"));

    public static final StreamCodec<ByteBuf, DynamicBrightPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT,
                    DynamicBrightPayload::bright,
                    DynamicBrightPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

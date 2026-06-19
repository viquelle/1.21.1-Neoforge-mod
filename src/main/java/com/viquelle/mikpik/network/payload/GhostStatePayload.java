package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GhostStatePayload(boolean isGhost) implements CustomPacketPayload {
    public static final Type<GhostStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "ghost_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    GhostStatePayload::isGhost,
                    GhostStatePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

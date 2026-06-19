package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ReviveRequestPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReviveRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "revive_request"));

    public static final ReviveRequestPayload INSTANCE = new ReviveRequestPayload();

    public static final StreamCodec<RegistryFriendlyByteBuf, ReviveRequestPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

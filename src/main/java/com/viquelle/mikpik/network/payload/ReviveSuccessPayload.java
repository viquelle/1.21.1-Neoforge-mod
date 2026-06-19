package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ReviveSuccessPayload implements CustomPacketPayload {

    public static final Type<ReviveSuccessPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "revive_success"));

    public static final ReviveSuccessPayload INSTANCE = new ReviveSuccessPayload();


    public static final StreamCodec<RegistryFriendlyByteBuf, ReviveSuccessPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
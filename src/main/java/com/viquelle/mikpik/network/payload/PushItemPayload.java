package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class PushItemPayload implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PushItemPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "push_item"));

    public static final PushItemPayload INSTANCE = new PushItemPayload();

    public static final StreamCodec<RegistryFriendlyByteBuf, PushItemPayload> STREAM_CODEC =
            StreamCodec.unit(PushItemPayload.INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

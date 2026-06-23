package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class GhostRespawnRequest implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GhostRespawnRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "ghost_respawn_request"));

    public static final GhostRespawnRequest INSTANCE = new GhostRespawnRequest();

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostRespawnRequest> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

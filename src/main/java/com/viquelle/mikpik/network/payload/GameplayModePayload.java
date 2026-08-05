package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.world.Gameplay;
import com.viquelle.mikpik.world.GameplayMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GameplayModePayload(GameplayMode mode) implements CustomPacketPayload {

    public static final Type<GameplayModePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "gameplay_mode"));

    public static final StreamCodec<FriendlyByteBuf, GameplayModePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeEnum(packet.mode),
                    buf -> new GameplayModePayload(buf.readEnum(GameplayMode.class))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
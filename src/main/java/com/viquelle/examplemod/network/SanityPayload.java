package com.viquelle.examplemod.network;


import com.viquelle.examplemod.ExampleMod;
import com.viquelle.examplemod.client.ClientPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SanityPayload(float sanity) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "sanity_sync");
    public static final CustomPacketPayload.Type<SanityPayload> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<ByteBuf, SanityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SanityPayload::sanity,
            SanityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            System.out.println("Received sanity sync packet: " + sanity);
            ClientPayloadHandler.handleSanityData(new SanityPayload(sanity), context);
        });
    }
}

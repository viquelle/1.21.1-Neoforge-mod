package com.viquelle.mikpik.sanity;

import com.mojang.serialization.Codec;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.event.shadow.ShadowedBlocksConsumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MikpikMod.MODID);

    public static final Supplier<AttachmentType<Float>> SANITY = ATTACHMENT_TYPES.register(
            "sanity", () -> AttachmentType.builder(() -> 100.0f)
                    .serialize(Codec.FLOAT)
                    .build()
    );

    public static final Supplier<AttachmentType<Float>> PENALTY = ATTACHMENT_TYPES.register(
            "penalty", () -> AttachmentType.builder(() -> 0.0f)
                    .serialize(Codec.FLOAT)
                    .build()
    );

    public static final Supplier<AttachmentType<String>> ACTIVE_PLUSHY_ID = ATTACHMENT_TYPES.register(
            "active_plushy_id",
            () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build()
    );

    public static final Supplier<AttachmentType<ShadowedBlocksConsumer>> SHADOWED_BLOCKS = ATTACHMENT_TYPES.register(
            "shadowed_blocks",
            () -> AttachmentType.builder(ShadowedBlocksConsumer::new)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> IS_GHOST = ATTACHMENT_TYPES.register(
            "is_ghost",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .sync(ByteBufCodecs.BOOL)
                    .build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}

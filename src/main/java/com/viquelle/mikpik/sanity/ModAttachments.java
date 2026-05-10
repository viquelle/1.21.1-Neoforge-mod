package com.viquelle.mikpik.sanity;

import com.mojang.serialization.Codec;
import com.viquelle.mikpik.MikpikMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MikpikMod.MODID);

    public static final Supplier<AttachmentType<Float>> SANITY = ATTACHMENT_TYPES.register(
            "sanity", () -> AttachmentType.builder(() -> 50.0f)
                    .serialize(Codec.FLOAT)
                    .build()
    );

    public static final Supplier<AttachmentType<String>> ACTIVE_PLUSHY_ID = ATTACHMENT_TYPES.register(
            "active_plushy_id",
            () -> AttachmentType.builder(() -> "")
                    .serialize(Codec.STRING)
                    .build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}

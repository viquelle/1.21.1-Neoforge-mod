package com.viquelle.examplemod.capability;

import com.viquelle.examplemod.ExampleMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.viquelle.examplemod.data.SanityData;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ExampleMod.MODID);

    public static final Supplier<AttachmentType<SanityData>> SANITY =
            ATTACHMENT_TYPES.register("sanity_data", () ->
                    AttachmentType.builder(() -> SanityData.DEFAULT).serialize(SanityData.CODEC)
                            .build());
//
//    public static final Supplier<AttachmentType<LightData>> FLASHLIGHT =
//            ATTACHMENT_TYPES.register("flashlight_data", () ->
//                    AttachmentType.builder(() -> LightData.DEFAULT).serialize(LightData.CODEC)
//                            .build());
}

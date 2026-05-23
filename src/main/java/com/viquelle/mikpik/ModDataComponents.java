package com.viquelle.mikpik;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(MikpikMod.MODID);

    public static final Supplier<DataComponentType<Float>> PLUSHY_STORED_SANITY =
            COMPONENTS.registerComponentType(
                    "pibble_stored_sanity",
                    floatBuilder -> floatBuilder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
            );

    public static final Supplier<DataComponentType<String>> PLUSHY_ID =
            COMPONENTS.registerComponentType(
                    "pibble_id",
                    builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.fromCodec(Codec.STRING))
            );

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}

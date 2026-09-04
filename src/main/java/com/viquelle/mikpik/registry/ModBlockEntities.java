package com.viquelle.mikpik.registry;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.blockentity.MeatEffigyBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MikpikMod.MODID);

    public static final Supplier<BlockEntityType<MeatEffigyBlockEntity>> MEAT_EFFIGY =
            BLOCK_ENTITIES.register(
                    "meat_effigy",
                    () -> BlockEntityType.Builder.of(
                            MeatEffigyBlockEntity::new,
                            ModBlocks.MEAT_EFFIGY.get()
                    ).build(null)
            );
}

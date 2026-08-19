package com.viquelle.mikpik.registry;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.block.meateffigy.MeatEffigyBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MikpikMod.MODID);

    public static final DeferredHolder<Block, MeatEffigyBlock> MEAT_EFFIGY =
            BLOCKS.register("meat_effigy",
                    () -> new MeatEffigyBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(2.0F)
                                    .noOcclusion()
                                    .sound(SoundType.WOOD)
                    )
            );
}

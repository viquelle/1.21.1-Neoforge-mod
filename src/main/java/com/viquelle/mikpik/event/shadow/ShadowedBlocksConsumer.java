package com.viquelle.mikpik.event.shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadowedBlocksConsumer {

    public static class ShadowedBlock {
        public final Block block;
        public int ticks;

        public ShadowedBlock(Block block) {
            this.block = block;
            this.ticks = 0;
        }
    }

    public Map<BlockPos, ShadowedBlock> shadowed = new HashMap<>();
    public List<ShadowAnt> activeAnts = new ArrayList<>();
}
package com.viquelle.mikpik.event.shadow;

import net.minecraft.server.level.ServerLevel;

import static com.viquelle.mikpik.registry.ModAttachments.SHADOWED_BLOCKS;

public class ShadowWorld {
    public static ShadowedBlocksConsumer get(ServerLevel level) {
        return level.getData(SHADOWED_BLOCKS.get());
    }
}

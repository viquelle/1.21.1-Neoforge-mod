package com.viquelle.mikpik.client.coloredlights;

import com.viquelle.mikpik.MikpikMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.world.level.chunk.LevelChunk;

@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public final class ColoredLightEvents {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        if (!chunk.getLevel().isClientSide()) {
            return;
        }

        ColoredLightScanner.onChunkLoaded(chunk);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        ColoredLightScanner.onChunkUnloaded(
                event.getChunk().getPos()
        );
    }
}
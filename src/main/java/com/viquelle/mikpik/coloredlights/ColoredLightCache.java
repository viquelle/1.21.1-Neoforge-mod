package com.viquelle.mikpik.coloredlights;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class ColoredLightCache {

    public static final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<ActiveLight>> CHUNK_LIGHTS =
            new Long2ObjectOpenHashMap<>();

    private ColoredLightCache() {}

    public static Long2ObjectOpenHashMap<ActiveLight> getLightChunk(long chunkKey) {
        return CHUNK_LIGHTS.get(chunkKey);
    }
}
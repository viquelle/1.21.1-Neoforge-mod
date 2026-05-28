package com.viquelle.mikpik.coloredlights;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class ColoredLightCache {
    // Хранилище всех источников света: ключ чанка -> карта позиций блоков -> свет
    public static final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<ActiveLight>> CHUNK_LIGHTS = new Long2ObjectOpenHashMap<>();

    private ColoredLightCache() {}

    public static void clear() {
        CHUNK_LIGHTS.clear();
    }

    public static int getTotalLightCount() {
        int total = 0;
        for (var chunkLights : CHUNK_LIGHTS.values()) {
            total += chunkLights.size();
        }
        return total;
    }
}
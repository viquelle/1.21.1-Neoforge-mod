package com.viquelle.mikpik.coloredlights;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

import java.util.HashMap;
import java.util.Map;


public final class HardcodedLights {

    public static final class LightData {
        public final int color;
        public final float radius;
        public final float intensity;

        public LightData(int color, float radius, float intensity) {
            this.color = color;
            this.radius = radius;
            this.intensity = intensity;
        }
    }

    private static final Map<Object, LightData> LIGHTS = new HashMap<>();

    static {

        LIGHTS.put(Blocks.TORCH, new LightData(0xFFD294, 15f, 1.f));
        LIGHTS.put(Blocks.WALL_TORCH, new LightData(0xFFD294, 15f, 1f));
        LIGHTS.put(Blocks.LANTERN, new LightData(0xFFD294, 15f, 1.f));

        LIGHTS.put(Blocks.SOUL_TORCH, new LightData(0x3d64FF, 11f, 0.8f));
        LIGHTS.put(Blocks.SOUL_WALL_TORCH, new LightData(0x3d64FF, 11f, 0.8f));
        LIGHTS.put(Blocks.SOUL_LANTERN, new LightData(0x3d64FF, 11f, 0.8f));

        LIGHTS.put(Blocks.REDSTONE_TORCH, new LightData(0xFF4040, 8f, 1.2f));
        LIGHTS.put(Blocks.REDSTONE_WALL_TORCH, new LightData(0xFF4040, 8f, 1.2f));

        LIGHTS.put(Blocks.MAGMA_BLOCK, new LightData(0xffa55c, 3f, 0.6f));
    }

    public static LightData get(Object block) {
        return LIGHTS.get(block);
    }
}
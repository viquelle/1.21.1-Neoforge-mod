package com.viquelle.mikpik.coloredlights;

import net.minecraft.world.level.block.Blocks;

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

    public static final Map<Object, LightData> LIGHTS = new HashMap<>();

    static {

        LIGHTS.put(Blocks.TORCH, new LightData(
                0xFFAA33,
                15f,
                1.0f
        ));

        LIGHTS.put(Blocks.SOUL_TORCH, new LightData(
                0x33BBFF,
                12f,
                1.0f
        ));

        LIGHTS.put(Blocks.REDSTONE_TORCH, new LightData(
                0xFF2222,
                8f,
                1.0f
        ));

        LIGHTS.put(Blocks.MAGMA_BLOCK, new LightData(
                0xFF6600,
                6f,
                0.6f
        ));
    }

    public static LightData get(Object block) {
        return LIGHTS.get(block);
    }
}
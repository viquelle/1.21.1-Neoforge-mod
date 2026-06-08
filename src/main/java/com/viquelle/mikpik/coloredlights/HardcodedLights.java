package com.viquelle.mikpik.coloredlights;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public final class HardcodedLights {

    public static final class LightData {
        public final int color;
        public final float radius;
        public final float intensity;
        public final boolean isRepresentative;

        public LightData(int color, float radius, float intensity, boolean isRepresentative) {
            this.color = color;
            this.radius = radius;
            this.intensity = intensity;
            this.isRepresentative = isRepresentative;
        }
    }

    private static final Map<Block, LightData> LIGHTS = new HashMap<>();

    static {
        LIGHTS.put(Blocks.TORCH, new LightData(0xFFD294, 15f, 1f, false));
        LIGHTS.put(Blocks.WALL_TORCH, new LightData(0xFFD294, 15f, 1f, false));
        LIGHTS.put(Blocks.LANTERN, new LightData(0xFFD294, 15f, 1f, false));

        LIGHTS.put(Blocks.SOUL_TORCH, new LightData(0x3d64FF, 11f, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_WALL_TORCH, new LightData(0x3d64FF, 11f, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_LANTERN, new LightData(0x3d64FF, 11f, 1.0f, false));

        LIGHTS.put(Blocks.REDSTONE_TORCH, new LightData(0xFF4040, 7f, 1.0f, false));
        LIGHTS.put(Blocks.REDSTONE_WALL_TORCH, new LightData(0xFF4040, 7f, 1.0f, false));

        LIGHTS.put(Blocks.SEA_LANTERN, new LightData(0x75FFDB, 15f, 1.0f, false));

        LIGHTS.put(Blocks.OCHRE_FROGLIGHT, new LightData(0xFFEF80, 15f, 1.0f, false));
        LIGHTS.put(Blocks.VERDANT_FROGLIGHT, new LightData(0x91FFAE, 15f, 1.0f, false));
        LIGHTS.put(Blocks.PEARLESCENT_FROGLIGHT, new LightData(0xFFA3DF, 15f, 1.0f, false));

        LIGHTS.put(Blocks.SMALL_AMETHYST_BUD, new LightData(0xCFB0FF, 1f, 1.f, false));
        LIGHTS.put(Blocks.MEDIUM_AMETHYST_BUD, new LightData(0xCFB0FF, 2f, 1.33f, false));
        LIGHTS.put(Blocks.LARGE_AMETHYST_BUD, new LightData(0xCFB0FF, 3.f, 1.66f, false));
        LIGHTS.put(Blocks.AMETHYST_CLUSTER, new LightData(0xCFB0FF, 3.5f, 2f, false));
//        LIGHTS.put(Blocks.LAVA, new LightData(0xffa55c, 6f, 1.0f, true)); // idk its absurd its too hard for normal perfomance
//        LIGHTS.put(Blocks.MAGMA_BLOCK, new LightData(0xffa55c, 3f, 0.6f, true)); // so maybe later
    }

    public static LightData get(Block block) {
        return LIGHTS.get(block);
    }
}
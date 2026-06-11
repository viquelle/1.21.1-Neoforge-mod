package com.viquelle.mikpik.coloredlights;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class HardcodedLights {

    public record LightDefinition(
            Predicate<BlockState> predicate,
            LightData data
    ) {}

    private static LightDefinition light(int color, float radius, float intensity, boolean representative) {
        return new LightDefinition(
                state -> true,
                new LightData(color, radius, intensity, representative)
        );
    }

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

    private static final Map<Block, LightDefinition> LIGHTS = new HashMap<>();

    static {
        LIGHTS.put(Blocks.TORCH, light(0xFFD294, 15f, 1f, false));
        LIGHTS.put(Blocks.WALL_TORCH, light(0xFFD294, 15f, 1f, false));
        LIGHTS.put(Blocks.LANTERN, light(0xFFD294, 15f, 1f, false));
        LIGHTS.put(Blocks.FIRE, light(0xFFD294, 15f, 1f, false));
        LIGHTS.put(
                Blocks.CAMPFIRE,
                new LightDefinition(
                        state -> state.getValue(CampfireBlock.LIT),
                        new LightData(0xFFD294, 15f, 1f, false)
                )
        );

        LIGHTS.put(Blocks.SOUL_TORCH, light(0x3d64FF, 11f, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_WALL_TORCH, light(0x3d64FF, 11f, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_LANTERN, light(0x3d64FF, 11f, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_FIRE, light(0x3d64FF, 11f, 1.0f, false));
        LIGHTS.put(
                Blocks.SOUL_CAMPFIRE,
                new LightDefinition(
                        state -> state.getValue(CampfireBlock.LIT),
                        new LightData(0x3d64FF, 11f, 1.0f, false)
                )
        );

        LIGHTS.put(
                Blocks.REDSTONE_TORCH,
                new LightDefinition(
                        state -> state.getValue(RedstoneTorchBlock.LIT),
                        new LightData(0xFF4040, 8f, 1.0f, false)
                )
        );

        LIGHTS.put(
                Blocks.REDSTONE_WALL_TORCH,
                new LightDefinition(
                        state -> state.getValue(RedstoneWallTorchBlock.LIT),
                        new LightData(0xFF4040, 8f, 1.0f, false)
                )
        );

        LIGHTS.put(
                Blocks.REDSTONE_ORE,
                new LightDefinition(
                        state -> state.getValue(RedStoneOreBlock.LIT),
                        new LightData(0xFF4040, 8f, 1.0f, true)
                )
        );

        LIGHTS.put(
                Blocks.DEEPSLATE_REDSTONE_ORE,
                new LightDefinition(
                        state -> state.getValue(RedStoneOreBlock.LIT),
                        new LightData(0xFF4040, 8f, 1.0f, true)
                )
        );

        LIGHTS.put(Blocks.SEA_LANTERN, light(0x75FFDB, 15f, 1.0f, true));
        LIGHTS.put(
                Blocks.SCULK_SENSOR,
                new LightDefinition(
                        state -> state.getValue(SculkSensorBlock.PHASE) == SculkSensorPhase.ACTIVE,
                        new LightData(0x00F7FF, 2f, 1.0f, false)
                )
        );

        LIGHTS.put(
                Blocks.CALIBRATED_SCULK_SENSOR,
                new LightDefinition(
                        state -> state.getValue(CalibratedSculkSensorBlock.PHASE) == SculkSensorPhase.ACTIVE,
                        new LightData(0x00F7FF, 2f, 1.0f, false)
                )
        );

        LIGHTS.put(Blocks.OCHRE_FROGLIGHT, light(0xFFEF80, 15f, 1.0f, false));
        LIGHTS.put(Blocks.VERDANT_FROGLIGHT, light(0x91FFAE, 15f, 1.0f, false));
        LIGHTS.put(Blocks.PEARLESCENT_FROGLIGHT, light(0xFFA3DF, 15f, 1.0f, false));

        LIGHTS.put(Blocks.SMALL_AMETHYST_BUD, light(0xCFB0FF, 1f, 3.5f, false));
        LIGHTS.put(Blocks.MEDIUM_AMETHYST_BUD, light(0xCFB0FF, 2f, 3.5f, false));
        LIGHTS.put(Blocks.LARGE_AMETHYST_BUD, light(0xCFB0FF, 2.5f, 3.5f, false));
        LIGHTS.put(Blocks.AMETHYST_CLUSTER, light(0xCFB0FF, 3.5f, 3.5f, false));
//        LIGHTS.put(Blocks.LAVA, new LightData(0xffa55c, 6f, 1.0f, true)); // idk its absurd its too hard for normal perfomance
//        LIGHTS.put(Blocks.MAGMA_BLOCK, new LightData(0xffa55c, 3f, 0.6f, true)); // so maybe later
    }

    public static LightDefinition get(Block block) {
        return LIGHTS.get(block);
    }
}
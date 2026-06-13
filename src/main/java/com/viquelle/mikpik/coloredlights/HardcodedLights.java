package com.viquelle.mikpik.coloredlights;

import com.viquelle.mikpik.darknesscomputer.Darkness;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
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

    private static LightDefinition basicLight(int color, float intensity, boolean representative) {
        return new LightDefinition(
                state -> true,
                new LightData(color, intensity, representative)
        );
    }

    public static final class LightData {
        public final int color;
        public final float intensity;
        public final boolean isRepresentative;

        public LightData(int color, float intensity, boolean isRepresentative) {
            this.color = color;
            this.intensity = intensity;
            this.isRepresentative = isRepresentative;
        }
    }

    private static final Map<Block, LightDefinition> LIGHTS = new HashMap<>();

    static {
        // Факелы
        LIGHTS.put(Blocks.TORCH, basicLight(0xFFD294, 1f, false));
        LIGHTS.put(Blocks.WALL_TORCH, basicLight(0xFFD294, 1f, false));
        LIGHTS.put(Blocks.LANTERN, basicLight(0xFFD294, 1f, false));
        LIGHTS.put(Blocks.FIRE, basicLight(0xFFD294, 1f, false));

        LIGHTS.put(
                Blocks.CAMPFIRE,
                new LightDefinition(
                        state -> state.getValue(CampfireBlock.LIT),
                        new LightData(0xFFD294, 1f, false)
                )
        );

        // Soul источники
        LIGHTS.put(Blocks.SOUL_TORCH, basicLight(0x3d64FF, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_WALL_TORCH, basicLight(0x3d64FF, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_LANTERN, basicLight(0x3d64FF, 1.0f, false));
        LIGHTS.put(Blocks.SOUL_FIRE, basicLight(0x3d64FF, 1.0f, false));
        LIGHTS.put(
                Blocks.SOUL_CAMPFIRE,
                new LightDefinition(
                        state -> state.getValue(CampfireBlock.LIT),
                        new LightData(0x3d64FF, 1.0f, false)
                )
        );

        // Redstone источники
        LIGHTS.put(
                Blocks.REDSTONE_TORCH,
                new LightDefinition(
                        state -> state.getValue(RedstoneTorchBlock.LIT),
                        new LightData(0xFF4040, 1.0f, false)
                )
        );

        LIGHTS.put(
                Blocks.REDSTONE_WALL_TORCH,
                new LightDefinition(
                        state -> state.getValue(RedstoneWallTorchBlock.LIT),
                        new LightData(0xFF4040, 1.0f, false)
                )
        );

        LIGHTS.put(
                Blocks.REDSTONE_ORE,
                new LightDefinition(
                        state -> state.getValue(RedStoneOreBlock.LIT),
                        new LightData(0xFF4040, 1.0f, true)
                )
        );

        LIGHTS.put(
                Blocks.DEEPSLATE_REDSTONE_ORE,
                new LightDefinition(
                        state -> state.getValue(RedStoneOreBlock.LIT),
                        new LightData(0xFF4040, 1.0f, true)
                )
        );

        // Другие блоки
        LIGHTS.put(Blocks.SEA_LANTERN, basicLight(0x75FFDB, 1.0f, true));

        LIGHTS.put(
                Blocks.SCULK_SENSOR,
                new LightDefinition(
                        state -> state.getValue(SculkSensorBlock.PHASE) == SculkSensorPhase.ACTIVE,
                        new LightData(0x00F7FF, 1.0f, false)
                )
        );

        LIGHTS.put(
                Blocks.CALIBRATED_SCULK_SENSOR,
                new LightDefinition(
                        state -> state.getValue(CalibratedSculkSensorBlock.PHASE) == SculkSensorPhase.ACTIVE,
                        new LightData(0x00F7FF, 1.0f, false)
                )
        );

        // Froglight
        LIGHTS.put(Blocks.OCHRE_FROGLIGHT, basicLight(0xFFEF80, 1.0f, false));
        LIGHTS.put(Blocks.VERDANT_FROGLIGHT, basicLight(0x91FFAE, 1.0f, false));
        LIGHTS.put(Blocks.PEARLESCENT_FROGLIGHT, basicLight(0xFFA3DF, 1.0f, false));

        // Аметист
        LIGHTS.put(Blocks.SMALL_AMETHYST_BUD, basicLight(0xCFB0FF, 3.5f, false));
        LIGHTS.put(Blocks.MEDIUM_AMETHYST_BUD, basicLight(0xCFB0FF, 3.5f, false));
        LIGHTS.put(Blocks.LARGE_AMETHYST_BUD, basicLight(0xCFB0FF, 3.5f, false));
        LIGHTS.put(Blocks.AMETHYST_CLUSTER, basicLight(0xCFB0FF, 3.5f, false));
    }

    public static LightDefinition get(Block block) {
        return LIGHTS.get(block);
    }
}
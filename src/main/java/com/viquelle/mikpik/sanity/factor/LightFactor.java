package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class LightFactor implements SanityFactor {
    @Override
    public float getDelta(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);

        // WIP: потом сюда добавишь Veil light
        int veilLight = 0;

        int maxLight = Math.max(blockLight, veilLight);

        // Дневной свет неба регенит рассудок.
        if (level.isDay() && skyLight > 8) {
            return SanityConstants.perTick(SanityConstants.SKY_DAY_REGEN_PER_SECOND);
        }

        // Темнота отнимает рассудок.
        // Тут специально используем maxLocalRawBrightness,
        // потому что он лучше отражает фактическую локальную яркость.
        int localLight = level.getMaxLocalRawBrightness(pos);
        int effectiveDarknessLight = Math.max(localLight, veilLight);

        if (effectiveDarknessLight <= 5) {
            return SanityConstants.perTick(SanityConstants.DARK_DRAIN_PER_SECOND);
        }

        return 0.0f;
    }
}
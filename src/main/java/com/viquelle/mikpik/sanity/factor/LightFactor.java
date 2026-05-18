package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.light.ClientLightManager;
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

        int veilLight = Math.round(ClientLightManager.sampleLight(pos.getCenter()) * SanityConstants.VEIL_NORMALIZATION);

        // Дневной свет неба регенит рассудок.
        if (level.isDay() && skyLight > 8) {
            return SanityConstants.SKY_DAY_REGEN_PER_TICK;
        }

        // Темнота отнимает рассудок.
        // Тут используем maxLocalRawBrightness,
        // потому что он лучше отражает фактическую локальную яркость.

        int localLight = level.getMaxLocalRawBrightness(pos);
        int effectiveDarknessLight = Math.max(localLight, veilLight);

        if (effectiveDarknessLight <= SanityConstants.BRIGHTNESS_THRESHOLD) {
            return SanityConstants.DARK_DRAIN_PER_TICK;
        }

        return 0.0f;
    }
}
package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.darknesscomputer.Darkness;
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

        float skyLight = Darkness.playerSkyLight(player, level, 0f);

        float veilLight = Math.round(ClientLightManager.sampleLight(pos.getCenter()) * SanityConstants.VEIL_NORMALIZATION);

        // Дневной свет неба регенит рассудок.
        if (level.isDay() && skyLight > 8f) {
            return SanityConstants.SKY_DAY_REGEN_PER_TICK;
        }

        float localLight = Math.max(skyLight, level.getBrightness(LightLayer.BLOCK, pos));
        float effectiveDarknessLight = Math.max(localLight, veilLight);

        if (effectiveDarknessLight <= SanityConstants.BRIGHTNESS_THRESHOLD) {
            return SanityConstants.DARK_DRAIN_PER_TICK;
        }

        return 0.0f;
    }
}
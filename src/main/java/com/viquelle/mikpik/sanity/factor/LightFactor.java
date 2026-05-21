package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.darknesscomputer.Darkness;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class LightFactor implements SanityFactor {
    @Override
    public float getDelta(Player player) {
        Level level = player.level();
        Vec3 pos = player.getEyePosition(1f);

        float skyLight = Darkness.posSkyLight(pos, level, 1f);

        // Дневной свет неба регенит рассудок.
        if (level.isDay() && skyLight > 8f) {
            return SanityConstants.SKY_DAY_REGEN_PER_TICK;
        }

        float blocklight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(pos));
        if (ClientLightManager.isDarkOnPos(pos, level, 1f)) {
            return SanityConstants.DARK_DRAIN_PER_TICK;
        } else if (skyLight <= SanityConstants.BRIGHTNESS_THRESHOLD || blocklight <= SanityConstants.BRIGHTNESS_THRESHOLD) {
            return SanityConstants.DARK_DRAIN_PER_TICK / 3.f;
        }

        return 0.0f;
    }
}
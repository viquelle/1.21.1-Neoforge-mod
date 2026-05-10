package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;

public class ShelterFactor implements SanityFactor {
    @Override
    public float getDelta(Player player) {
        if (!isSheltered(player)) {
            return 0.0f;
        }

        Level level = player.level();
        BlockPos pos = player.blockPosition();

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);

        // В укрытии должен быть свет. Иначе это не safe shelter, а просто нора.
        if (blockLight < 7) {
            return 0.0f;
        }

        return SanityConstants.perTick(SanityConstants.SHELTER_REGEN_PER_SECOND);
    }

    public static boolean isSheltered(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        if (level.canSeeSky(pos)) {
            return false;
        }

        int covered = 0;
        int checked = 0;

        int radius = 2;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                checked++;

                BlockPos checkPos = pos.offset(dx,0,dz);

                if (!level.canSeeSky(checkPos)) {
                    covered++;
                }
            }
        }

        float coverRatio = checked == 0 ? 0.0f : (float) covered / checked;

        return coverRatio >= 0.75f;
    }
}
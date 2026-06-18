package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class NearbyPlayersAndGhostFactor implements SanityFactor {
    private static final double RADIUS = 8.0;

    @Override
    public float getDelta(Player player) {
        AABB area = player.getBoundingBox().inflate(RADIUS);

        List<Player> nearby = player.level().getEntitiesOfClass(
                Player.class,
                area,
                other -> other != player && other.isAlive()
        );

        if (nearby.isEmpty()) {
            return 0.0f;
        }

        float total = 0.0f;

        for (Player other : nearby) {
            double dist = player.distanceTo(other);
            float closeness = (float) (1.0 - Math.min(dist / RADIUS, 1.0));

            if (GhostManager.isGhost(other)) {
                total += SanityConstants.NEAR_GHOSTPLAYER_DRAIN_PER_TICK * closeness;
            } else {
                total += SanityConstants.NEAR_PLAYER_REGEN_PER_TICK * closeness;
            }
        }

        // Чтобы пачка игроков или призраков не разгоняла реген или понижение в космос.
        total = Math.max(Math.min(total, SanityConstants.NEAR_PLAYER_REGEN_PER_TICK), SanityConstants.NEAR_GHOSTPLAYER_DRAIN_PER_TICK);

        return total;
    }
}
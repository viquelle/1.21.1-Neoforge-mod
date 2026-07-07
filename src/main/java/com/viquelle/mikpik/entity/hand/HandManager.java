package com.viquelle.mikpik.entity.hand;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ghost.GhostManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class HandManager {

    private static final Map<UUID, Set<HandEntity>> HANDS = new HashMap<>();

    public static void register(HandEntity hand, LivingEntity target) {
        HANDS.computeIfAbsent(target.getUUID(), k -> new HashSet<>()).add(hand);
    }

    public static void unregister(HandEntity hand, LivingEntity target) {
        if (target == null) return;

        Set<HandEntity> set = HANDS.get(target.getUUID());
        if (set != null) {
            set.remove(hand);
            if (set.isEmpty()) {
                HANDS.remove(target.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void server(LevelTickEvent.Post event) {
        serverTick(event.getLevel());
    }

    public static void serverTick(Level level) {
        if (!(level instanceof ServerLevel)) return;

        cleanup();
        damageTick(level);
    }

    public static int getHandCount(LivingEntity entity) {
        Set<HandEntity> set = HANDS.get(entity.getUUID());
        return set == null ? 0 : set.size();
    }

    private static void cleanup() {
        for (Iterator<Map.Entry<UUID, Set<HandEntity>>> it = HANDS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Set<HandEntity>> entry = it.next();
            Set<HandEntity> set = entry.getValue();

            set.removeIf(hand ->
                    hand == null
                            || !hand.isAlive()
            );

            if (set.isEmpty()) {
                it.remove();
            }
        }
    }

    private static void damageTick(Level level) {
        for (Iterator<Map.Entry<UUID, Set<HandEntity>>> it = HANDS.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, Set<HandEntity>> entry = it.next();
            Player player = level.getPlayerByUUID(entry.getKey());

            if (player == null) return;
            if (GhostManager.isGhost(player)) continue;

            Set<HandEntity> hands = entry.getValue();
            if (hands == null || hands.isEmpty()) {
                it.remove();
                return;
            }

            int count = getHandCount(player);
            float damate = computeDamage(count);

            if (damate > 0) {
                player.hurt(player.damageSources().generic(), damate);
            }
        }
    }

    private static float computeDamage(int hands) {

        if (hands <= 1) return 0f;

        if (hands <= 5) {
            return (hands - 1) * 0.6f;
        }

        return 2.4f + (hands - 5) * 3.5f;
    }

    public static Set<HandEntity> getHands(LivingEntity entity) {
        return HANDS.getOrDefault(entity.getUUID(), Collections.emptySet());
    }

    public static void clearHands(LivingEntity entity) {
        Set<HandEntity> set = HANDS.remove(entity.getUUID());
        if (set != null) {
            for (HandEntity hand : set) {
                hand.setTarget(null);
            }
        }
    }
}
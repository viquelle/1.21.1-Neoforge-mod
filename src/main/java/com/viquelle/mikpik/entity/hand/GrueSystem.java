package com.viquelle.mikpik.entity.hand;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.entity.ModEntities;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.light.ServerLightManager;
import com.viquelle.mikpik.sanity.SanitySystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class GrueSystem
{
    private static final int INITIAL_DARK_TICKS = 80;

    private static final int MIN_SPAWN_INTERVAL = 20;
    private static final int MAX_SPAWN_INTERVAL = 40;

    private static final Map<UUID, GrueRuntime> RUNTIME = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.isCreative() || player.isSpectator() || !player.isAlive() || GhostManager.isGhost(player)) {
            RUNTIME.remove(player.getUUID());
            HandManager.clearHands(player);
            return;
        }

        if (SanitySystem.get(player) > 20) return;

        boolean isDark = ServerLightManager.isPlayerInDark(player);
        GrueRuntime runtime = RUNTIME.computeIfAbsent(player.getUUID(), id -> new GrueRuntime());

        if (!isDark) {
            if (runtime.darkTicks > 0 || !HandManager.getHands(player).isEmpty()) {
                runtime.reset();
                HandManager.clearHands(player);
            }
            return;
        }

        runtime.darkTicks++;
        if (runtime.darkTicks < INITIAL_DARK_TICKS) {
            return;
        }

        if (!runtime.hasSpawnedFirst) {
            spawnGrueHand(player);
            runtime.hasSpawnedFirst = true;
            runtime.nextSpawnIn = randomInterval();
        } else {
            runtime.nextSpawnIn--;
            if (runtime.nextSpawnIn <= 0) {
                spawnGrueHand(player);
                runtime.nextSpawnIn = randomInterval();
            }
        }
    }

    private static void spawnGrueHand(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
        double dist = 0.5 + ThreadLocalRandom.current().nextDouble();
        double x = player.getX() + Math.cos(angle) * dist;
        double z = player.getZ() + Math.sin(angle) * dist;
        double y = player.getY();

        Vec3 spawnPos = new Vec3(x, y, z);

        HandEntity hand = new HandEntity(ModEntities.HAND.get(), level);
        hand.setPos(spawnPos);
        hand.setOrigin(spawnPos);
        hand.setTarget(player);
        hand.setState(HandEntity.State.REACHING);

        level.addFreshEntity(hand);

        float pitch = 0.5f + ThreadLocalRandom.current().nextFloat() * 0.3f;
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.2f, pitch);
    }

    private static int randomInterval() {
        return ThreadLocalRandom.current().nextInt(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL + 1);
    }

    private static final class GrueRuntime {
        int darkTicks = 0;
        int nextSpawnIn = 0;
        boolean hasSpawnedFirst = false;

        void reset() {
            darkTicks = 0;
            nextSpawnIn = 0;
            hasSpawnedFirst = false;
        }
    }
}
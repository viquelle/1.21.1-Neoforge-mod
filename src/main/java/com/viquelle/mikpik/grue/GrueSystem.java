package com.viquelle.mikpik.grue;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.sanity.SanitySystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.LightLayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = MikpikMod.MODID)
public final class GrueSystem {
    private static final float SANITY_THRESHOLD = 20.0f;
    private static final int LIGHT_THRESHOLD = 2;

    private static final int DARK_TICKS_REQUIRED = 3 * 20;

    private static final int MIN_ATTACK_SOUND_TICKS = 2 * 20;
    private static final int MAX_ATTACK_SOUND_TICKS = 3 * 20;
    private static final int HIT_EXTRA_TICKS = 10;

    private static final float LOCK_PROGRESS = 0.80f;
    private static final float DAMAGE = 14f;

    private static final Map<UUID, GrueRuntime> RUNTIME = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.isCreative() || player.isSpectator() || !player.isAlive()) {
            RUNTIME.remove(player.getUUID());
            return;
        }

        float sanity = SanitySystem.get(player);

        // Игрок в безопасном по рассудку состоянии — проверяем редко
        if (sanity > SANITY_THRESHOLD) {
            if (player.tickCount % 20 != 0) {
                return;
            }

            RUNTIME.remove(player.getUUID());
            return;
        } else {
            // sanity <= 20 — уже опасное состояние, проверяем каждый тик
            GrueRuntime runtime = RUNTIME.computeIfAbsent(player.getUUID(), id -> new GrueRuntime());

            tickLowSanityPlayer(player, runtime);
        }
    }

    private static void tickLowSanityPlayer(ServerPlayer player, GrueRuntime runtime) {
        boolean dark = isGrueDark(player);

        if (!runtime.attacking) {
            if (!dark) {
                runtime.darkTicks = 0;
                return;
            } else runtime.darkTicks++;

            if (runtime.darkTicks >= DARK_TICKS_REQUIRED) {
                startAttack(player, runtime);
            }

        } else {
            tickAttack(player, runtime, dark);
        }
    }

    private static void startAttack(ServerPlayer player, GrueRuntime runtime) {
        runtime.attacking = true;
        runtime.attackTicks = 0;
        runtime.locked = false;

        runtime.attackSoundTicks = ThreadLocalRandom.current().nextInt(
                MIN_ATTACK_SOUND_TICKS,
                MAX_ATTACK_SOUND_TICKS + 1
        );

        runtime.pitch = calculatePitch(runtime.attackSoundTicks);

        playAttackSound(player, runtime.pitch, 1.5f);
    }

    private static void tickAttack(ServerPlayer player, GrueRuntime runtime, boolean dark) {
        runtime.attackTicks++;

        float progress = runtime.attackTicks / (float) runtime.attackSoundTicks;

        if (progress >= LOCK_PROGRESS) {
            runtime.locked = true;
        }

        // До 80% звук/атака отменяются светом.
        if (!runtime.locked && !dark) {
            cancelAttack(player, runtime);
            return;
        }

        // Поддерживающий звук во время атаки.
        if (runtime.attackTicks % 8 == 0 && runtime.attackTicks <= runtime.attackSoundTicks) {
            float urgency = 1.0f + progress * 0.35f;
            playAttackSound(player, runtime.pitch * urgency, 1.3f);
        }

        int hitTick = runtime.attackSoundTicks + HIT_EXTRA_TICKS;

        if (runtime.attackTicks >= hitTick) {
            hit(player);
            reset(runtime);
        }
    }

    private static boolean isGrueDark(ServerPlayer player) {
        var level = player.level();
        var pos = player.blockPosition();

        int block = level.getBrightness(LightLayer.BLOCK, pos);

        if (block > LIGHT_THRESHOLD) {
            return false;
        }

        int raw = level.getMaxLocalRawBrightness(pos);

        return raw <= LIGHT_THRESHOLD;
    }

    private static float calculatePitch(int attackSoundTicks) {
        float t = (attackSoundTicks - MIN_ATTACK_SOUND_TICKS)
                / (float) (MAX_ATTACK_SOUND_TICKS - MIN_ATTACK_SOUND_TICKS);

        return 1.4f - t * 0.5f;
        // 2 сек => pitch 1.4
        // 3 сек => pitch 0.9
    }

    private static void playAttackSound(ServerPlayer player, float pitch, float volume) {
        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.WARDEN_SONIC_CHARGE,
                SoundSource.HOSTILE,
                volume,
                pitch
        );
    }

    private static void cancelAttack(ServerPlayer player, GrueRuntime runtime) {
        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.HOSTILE,
                0.8f,
                0.7f
        );

        reset(runtime);
    }

    private static void hit(ServerPlayer player) {
        var source = player.damageSources().source(DamageTypes.GENERIC);
        player.hurt(source, DAMAGE);
    }

    private static void reset(GrueRuntime runtime) {
        runtime.darkTicks = 0;
        runtime.attacking = false;
        runtime.attackTicks = 0;
        runtime.attackSoundTicks = 0;
        runtime.locked = false;
        runtime.pitch = 1.0f;
    }

    private static final class GrueRuntime {
        int darkTicks = 0;

        boolean attacking = false;
        boolean locked = false;

        int attackTicks = 0;
        int attackSoundTicks = 0;

        float pitch = 1.0f;
    }
}
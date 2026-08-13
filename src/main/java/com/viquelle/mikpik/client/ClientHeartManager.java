package com.viquelle.mikpik.client;

import com.viquelle.mikpik.client.darknesscomputer.Darkness;
import com.viquelle.mikpik.item.items.HeartItem;
import com.viquelle.mikpik.light.ClientLightManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClientHeartManager {
    public static Player renderingPlayer;
    private static final int GUI_STATE_ID = -1;
    private static final Map<Integer, HeartState> heartStateCache = new HashMap<>();

    public static class HeartState {
        public double lastUpdate = 0f;
        public float phaseTicks;

        public float pulseTicks;
        public float restTicks;

        private float lastPulsePercent = 0.0f;
        private boolean shouldBeat = false;
        private float volume = 0.0f;
        private float pitch = 0.0f;

        public HeartState(long gameTime, float partialTick, @Nullable Entity entity) {
            lastUpdate = gameTime + partialTick;
            phaseTicks = 0.0f;

            float light = getLight(entity, partialTick);
            pulseTicks = getPulseTicks(light);
            restTicks = getRestTicks(light);
        }

        public void tick(long time, float partialTick, @Nullable Entity entity) {
            double gameTime = time + partialTick;
            double delta = gameTime - lastUpdate;
            if (delta <= 0) return;
            phaseTicks += (float) delta;
            if (phaseTicks >= (pulseTicks + restTicks)) {
                phaseTicks %= (pulseTicks + restTicks);
                lastPulsePercent = 0f;
                updateLight(entity, partialTick);
            }

            if (phaseTicks < pulseTicks) {
                float percent = phaseTicks / pulseTicks;

                if (lastPulsePercent < 0.3F && percent >= 0.3F) {
                    shouldBeat = true;
                    volume = 0.20F;
                    pitch = 0.90F;
                }

                if (lastPulsePercent < 0.8F && percent >= 0.8F) {
                    shouldBeat = true;
                    volume = 0.12F;
                    pitch = 1.05F;
                }

                lastPulsePercent = percent;
            }

            lastUpdate = gameTime;
        }

        public void playBeat(@Nullable Entity entity) {
            if (!shouldBeat || entity == null) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();

            if (entity == mc.player) {
                entity.playSound(
                        SoundEvents.WOOD_HIT,
                        volume,
                        pitch
                );
            } else if (mc.level != null) {
                mc.level.playSound(mc.player,
                        entity.blockPosition(),
                        SoundEvents.WOOD_HIT,
                        SoundSource.NEUTRAL,
                        volume,
                        pitch
                );
            }

            shouldBeat = false;
        }

        public float getScale(){
            if (phaseTicks < pulseTicks) {
                float percent = phaseTicks / pulseTicks;


                if (percent <= 0.6f) return (float) (1.0F + 0.2F * Math.sin(Math.PI * percent / 0.6f));
                else return (float) (1.0F + 0.15 * Math.sin(Math.PI * (percent - 0.6f) / 0.4f));

            } else {
                return 1.0f;
            }
        }

        private void updateLight(@Nullable Entity entity, float partialTick) {
            float light = getLight(entity, partialTick);
            pulseTicks = getPulseTicks(light);
            restTicks = getRestTicks(light);
        }

        private float getLight(@Nullable Entity entity, float partialTick) {
            if (entity == null) return 13.0f;

            float skyLight = Darkness.posSkyLight(entity.blockPosition().getBottomCenter(), entity.level(), partialTick);
            float blockLight = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition());
            float veilLight = ClientLightManager.sampleLight(entity.blockPosition().getBottomCenter());
            return Math.max(Math.max(skyLight, blockLight), veilLight);
        }

    }

    public static float getInventoryScale(ItemStack stack, ItemDisplayContext displayContext) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 1.0F;

        long gameTime = mc.level.getGameTime();
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);

        HeartState state;
        if (renderingPlayer != null) {
            state = heartStateCache.computeIfAbsent(
                    renderingPlayer.getId(),
                    id -> new HeartState(gameTime, partialTick, renderingPlayer)
            );

            state.tick(gameTime,partialTick, renderingPlayer);
            return state.getScale();
        }

        if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            state = heartStateCache.computeIfAbsent(
                    mc.player.getId(),
                    id -> new HeartState(gameTime, partialTick, mc.player)
            );

            state.tick(gameTime,partialTick, mc.player);
            return state.getScale();
        }

        state = heartStateCache.computeIfAbsent(
                GUI_STATE_ID,
                id -> new HeartState(gameTime, partialTick, null)
        );

        state.tick(gameTime,partialTick, null);
        state.playBeat(null);
        return state.getScale();
    }

    public static float getWorldScale(ItemEntity entity, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 1.0F;

        long gameTime = mc.level.getGameTime();
        HeartState state = heartStateCache.computeIfAbsent(
                entity.getId(),
                id -> new HeartState(gameTime, partialTick, entity)
        );
        state.tick(gameTime, partialTick, entity);

        return state.getScale();
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null) {
            heartStateCache.clear();
            return;
        }

        long gameTime = mc.level.getGameTime();

        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();

        boolean hasHeart =
                mainHand.getItem() instanceof HeartItem && HeartItem.isCharged(mainHand)
                        || offHand.getItem() instanceof HeartItem && HeartItem.isCharged(offHand);

        if (hasHeart) {
            HeartState state = heartStateCache.computeIfAbsent(
                    mc.player.getId(),
                    id -> new HeartState(gameTime, 0.0F, mc.player)
            );

            state.tick(gameTime, 0.0F, mc.player);
            state.playBeat(mc.player);
        }

        heartStateCache.values().removeIf(
                state -> state.lastUpdate
                        + state.restTicks
                        + state.pulseTicks
                        + 1F < gameTime
        );
    }

    public static void tickItemEntity(ItemEntity entity) {
        ItemStack stack = entity.getItem();

        if (!(stack.getItem() instanceof HeartItem)) return;
        if (!HeartItem.isCharged(stack)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        long gameTime = mc.level.getGameTime();

        HeartState state = heartStateCache.computeIfAbsent(
                entity.getId(),
                id -> new HeartState(gameTime, 0.0F, entity)
        );

        state.tick(gameTime, 0.0F, entity);
        state.playBeat(entity);
    }

    public static float getPulseTicks(float light) {
        return 7.0F + 3.0F * light / 15.0F;
    }

    public static float getRestTicks(float light) {
        return 3.0F + 7.0F * light / 15.0F;
    }
}
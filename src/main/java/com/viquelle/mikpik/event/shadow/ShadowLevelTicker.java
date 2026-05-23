package com.viquelle.mikpik.event.shadow;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.sanity.SanitySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.viquelle.mikpik.sanity.SanityConstants.MAX_SANITY;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class ShadowLevelTicker {
    private static final int ANT_SPAWN_INTERVAL = 20; // Спавн раз в 1 секунду
    private static int tickCounter = 0;
    private static final int GLOBAL_ANT_CAP = 10;     // Лимит на измерение
    private static final float MIN_SURFACE_CHANCE = 0.000370f;
    private static final float MAX_SURFACE_CHANCE = 0.003333f;
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        tickCounter++;
        updateAntsAndShadows(level);

        // Спавн новых муравьёв раз в секунду
        if (tickCounter % ANT_SPAWN_INTERVAL == 0) {
            runSampling(level);
        }
    }

    private static void runSampling(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;

        ShadowedBlocksConsumer data = ShadowWorld.get(level);

        if (data.activeAnts.size() >= GLOBAL_ANT_CAP) return;

        RandomSource random = level.random;
        // Пытаемся спавнить по 1 на игрока, пока не упремся в лимит
        for (ServerPlayer player : players) {
            if (data.activeAnts.size() >= GLOBAL_ANT_CAP) break;

            float sanity = SanitySystem.get(player);
            // Чем ниже рассудок, тем выше шанс спавна в этот тик
            float spawnChance = 1f - (sanity / MAX_SANITY);
            spawnChance *= spawnChance * spawnChance / 3f;
            boolean isCave = level.getBrightness(LightLayer.SKY, player.blockPosition()) <= 0;
            isCave &= player.blockPosition().getY() < 32f;
            spawnChance = isCave ? spawnChance : (float) Mth.lerp(spawnChance, MIN_SURFACE_CHANCE, MAX_SURFACE_CHANCE);
            if (random.nextFloat() < spawnChance) {
                data.activeAnts.add(new ShadowAnt(level, player, random));
            }
        }
    }

    private static void updateAntsAndShadows(ServerLevel level) {
        ShadowedBlocksConsumer data = ShadowWorld.get(level);

        data.activeAnts.removeIf(ant -> ant.tick(level));

        Iterator<Map.Entry<BlockPos, ShadowedBlocksConsumer.ShadowedBlock>> it =
                data.shadowed.entrySet().iterator();

        while (it.hasNext()) {
            var entry = it.next();
            BlockPos pos = entry.getKey();
            var value = entry.getValue();
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() != value.block) {
                explode(level, pos, true);
                it.remove();
                continue;
            }
            value.ticks++;
            wrap(level,pos, value.ticks);
            if (value.ticks >= 20) { finish(level, pos); it.remove(); }
        }
    }
    private static void wrap(ServerLevel level, BlockPos pos, int tick) {

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        double r = 0.6 + Math.sin(tick * 0.3) * 0.2;

        for (int i = 0; i < 6; i++) {

            double a = (Math.PI * 2 / 6) * i + tick * 0.2;

            level.sendParticles(
                    ParticleTypes.SMOKE,
                    x + Math.cos(a) * r,
                    y + Math.sin(tick * 0.4 + i) * 0.2,
                    z + Math.sin(a) * r,
                    1, 0, 0, 0, 0
            );
        }
    }

    private static void finish(ServerLevel level, BlockPos pos) {

        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof TorchBlock)) return;

        level.destroyBlock(pos, false);

        RandomSource random = level.random;
        if (level.random.nextFloat() >= 0.5f) {
            if (level.random.nextBoolean()) {
                Block.popResource(level, pos, new ItemStack(Items.STICK));
            } else {
                Block.popResource(level, pos, new ItemStack(Items.COAL));
            }
        }

        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                80,
                0.2, 0.2, 0.2,
                0.05
        );
    }

    private static void explode(ServerLevel level, BlockPos pos, boolean abrupt) {

        level.sendParticles(
                abrupt ? ParticleTypes.LARGE_SMOKE : ParticleTypes.LARGE_SMOKE,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                abrupt ? 40 : 80,
                0.2, 0.2, 0.2,
                abrupt ? 0.2 : 0.05
        );
    }
}
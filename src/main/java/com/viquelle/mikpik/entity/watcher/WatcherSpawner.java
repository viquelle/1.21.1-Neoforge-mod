package com.viquelle.mikpik.entity.watcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Random;

@EventBusSubscriber(Dist.CLIENT)
public class WatcherSpawner {

    private static int tickCounter = 0;
    private static int cooldown = 0;
    private static final Random random = new Random();

    private static final int CHECK_INTERVAL = 100; // Проверять каждые 5 секунд
    private static final int MIN_COOLDOWN = 600; // Минимум 30 секунд между спавнами
    private static final int MAX_COOLDOWN = 1200; // Максимум 60 секунд
    private static final double SPAWN_DISTANCE_MIN = 8.0;
    private static final double SPAWN_DISTANCE_MAX = 20.0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        tickCounter++;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (tickCounter % CHECK_INTERVAL != 0) return;

        trySpawnWatcher(mc.player, mc.level);
    }

    private static void trySpawnWatcher(LocalPlayer player, Level level) {
        BlockPos spawnPos = findSpawnPosition(player, level);
        if (spawnPos == null) return;

        WatcherEntity watcher = new WatcherEntity(
                com.viquelle.mikpik.entity.ModEntities.WATCHER.get(),
                level
        );
        watcher.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                random.nextFloat() * 360, 0);
        watcher.setMaxLifeTime(200 + random.nextInt(400)); // 10-30 секунд жизни
        level.addFreshEntity(watcher);

        // Установить кулдаун
        cooldown = MIN_COOLDOWN + random.nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
    }

    private static BlockPos findSpawnPosition(LocalPlayer player, Level level) {
        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();

        // Попытаться найти позицию за спиной или сбоку
        for (int attempt = 0; attempt < 10; attempt++) {
            double distance = SPAWN_DISTANCE_MIN + random.nextDouble() * (SPAWN_DISTANCE_MAX - SPAWN_DISTANCE_MIN);
            double angle = Math.PI + (random.nextDouble() - 0.5) * Math.PI; // За спиной (180° ± 90°)

            double dx = -lookVec.x * distance * Math.cos(angle) - lookVec.z * distance * Math.sin(angle);
            double dz = -lookVec.z * distance * Math.cos(angle) + lookVec.x * distance * Math.sin(angle);

            int x = Mth.floor(playerPos.x + dx);
            int y = Mth.floor(playerPos.y);
            int z = Mth.floor(playerPos.z + dz);

            BlockPos pos = new BlockPos(x, y, z);

            // Проверить что место тёмное и безопасное
            if (isValidSpawnPosition(pos, level, player)) {
                return pos;
            }
        }

        return null;
    }

    private static boolean isValidSpawnPosition(BlockPos pos, Level level, LocalPlayer player) {
        // Проверить что блок тёмный
        float light = level.getMaxLocalRawBrightness(pos);
        if (light > 0.3f) return false;

        // Проверить что есть воздух для сущности
        if (!level.getBlockState(pos).isAir()) return false;
        if (!level.getBlockState(pos.above()).isAir()) return false;

        // Проверить что не слишком близко к игроку
        double dist = pos.getCenter().distanceTo(player.position());
        if (dist < SPAWN_DISTANCE_MIN || dist > SPAWN_DISTANCE_MAX) return false;

        // Проверить что игрок не смотрит на эту позицию
        Vec3 toPos = pos.getCenter().subtract(player.getEyePosition()).normalize();
        double dot = player.getLookAngle().dot(toPos);
        if (dot > 0.5) return false; // Игрок смотрит в эту сторону

        return true;
    }
}
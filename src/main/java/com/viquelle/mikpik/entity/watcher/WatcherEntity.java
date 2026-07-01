package com.viquelle.mikpik.entity.watcher;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.MikpikModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import com.viquelle.mikpik.light.ClientLightManager;

public class WatcherEntity extends Entity {
    private BlockPos spawnPos;
    private BlockPos watchPos;
    private int lifeTime = 0;
    private int maxLifeTime;
    private int animationTimer = 0;

    public WatcherEntity(EntityType<? extends WatcherEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.maxLifeTime = 600 + level.random.nextInt(200);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public void tick() {
        if (!level().isClientSide) return;

        lifeTime++;
        animationTimer = (animationTimer + 1) % 200; // Цикл 10 секунд (200 тиков)

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean isClose = distanceTo(player) < 8.0;
        boolean isLit = !ClientLightManager.isDarkOnPos(blockPosition().getCenter(), level(), 1f);
        MikpikMod.LOGGER.info("{} || {} || {}", isClose, isLit, blockPosition().getCenter());
        if (isClose || isLit || lifeTime >= maxLifeTime) {
            discard();
        }
    }

    public int getBlinkFrame() {
        // 0-159 тиков (8 сек): кадр 0 (открыты)
        // 160-161 тиков (100мс): кадр 1 (закрываются)
        // 162-163 тика (100мс): кадр 2 (закрыты)
        // 164-165 тиков (100мс): кадр 3 (открываются)
        // 166-199 тиков: снова кадр 0

        if (animationTimer < 160) {
            return 0; // Открыты
        } else if (animationTimer < 162) {
            return 1; // Закрываются
        } else if (animationTimer < 164) {
            return 2; // Закрыты
        } else if (animationTimer < 166) {
            return 3; // Открываются
        } else {
            return 0; // Снова открыты
        }
    }

    public void setMaxLifeTime(int maxLifeTime) {
        this.maxLifeTime = maxLifeTime;
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}
}
package com.viquelle.mikpik.event.shadow;

import com.viquelle.mikpik.sanity.SanityConstants;
import com.viquelle.mikpik.sanity.SanitySystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class ShadowAnt {
    private BlockPos pos;
    private final ServerPlayer owner;
    private int age = 0;

    private static final int LIFESPAN = 40;
    private static final int SPAWN_RADIUS_XZ = 32;
    private static final int SPAWN_RADIUS_Y = 16;
    private static final int MAX_LIGHT = 14;

    public ShadowAnt(ServerLevel level, ServerPlayer owner, RandomSource random) {
        this.owner = owner;
        if (random.nextFloat() < 0.05f) {
            this.pos = owner.blockPosition();
        } else {
            int dx = random.nextInt(SPAWN_RADIUS_XZ * 2 + 1) - SPAWN_RADIUS_XZ;
            int dy = random.nextInt(SPAWN_RADIUS_Y * 2 + 1) - SPAWN_RADIUS_Y;
            int dz = random.nextInt(SPAWN_RADIUS_XZ * 2 + 1) - SPAWN_RADIUS_XZ;
            this.pos = owner.blockPosition().offset(dx, dy, dz).immutable();
        }
    }

    /** @return true если муравей умер и его можно удалить */
    public boolean tick(ServerLevel level) {
        if (age >= LIFESPAN) {
            dieAndTrigger(level, false);
            return true;
        }

        RandomSource random = level.random;
        int currentLight = level.getBrightness(LightLayer.BLOCK, pos);
        BlockPos bestPos = null;
        int bestLight = currentLight;

        // ищем соседа с бОльшим светом
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            int l = level.getBrightness(LightLayer.BLOCK, neighbor);
            if (l > bestLight) {
                bestLight = l;
                bestPos = neighbor;
            }
        }

        // движение
        if (bestPos != null) {
            // Нашли свет
            pos = bestPos;
        } else {
            // ползём к игроку по ОДНОЙ оси
            pos = stepTowardPlayer(owner.blockPosition(), random);
        }

//        level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
//                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
//                2, 0.05, 0.05, 0.05, 0.01);

        age++;

        // Если встал на макс. свет мгновенная атака
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (currentLight >= MAX_LIGHT - 8 && (block instanceof BaseTorchBlock || block instanceof LanternBlock)) {
            dieAndTrigger(level, true);
            return true;
        }
        return false;
    }

    /** Движение к цели по одной случайной оси (X, Y или Z) */
    private BlockPos stepTowardPlayer(BlockPos target, RandomSource random) {
        int dx = target.getX() - pos.getX();
        int dy = target.getY() - pos.getY();
        int dz = target.getZ() - pos.getZ();

        // Собираем оси, по которым есть смещение
        java.util.List<Character> axes = new java.util.ArrayList<>(3);
        if (dx != 0) axes.add('x');
        if (dy != 0) axes.add('y');
        if (dz != 0) axes.add('z');

        if (axes.isEmpty()) return pos; // Уже на позиции игрока

        // Выбираем случайную ось
        char axis = axes.get(random.nextInt(axes.size()));
        int step = switch (axis) {
            case 'x' -> Integer.signum(dx);
            case 'y' -> Integer.signum(dy);
            case 'z' -> Integer.signum(dz);
            default -> 0;
        };

        return switch (axis) {
            case 'x' -> pos.offset(step, 0, 0);
            case 'y' -> pos.offset(0, step, 0);
            case 'z' -> pos.offset(0, 0, step);
            default -> pos;
        };
    }

    public boolean isDead() { return age >= LIFESPAN; }

    private void dieAndTrigger(ServerLevel level, boolean foundTarget) {
        RandomSource random = level.random;
        float sanityFactor = 1f - (SanitySystem.get(owner) / SanityConstants.MAX_SANITY);
        ShadowedBlocksConsumer data = ShadowWorld.get(level);

        // Сначала проверяем текущую позицию
        if (tryBreakBlock(level, pos, foundTarget, data)) {
            return;
        }

        // Потом куб 5×5×5 вокруг
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    if (tryBreakBlock(level, checkPos, foundTarget, data)) {
                        return;
                    }
                }
            }
        }
    }

    private boolean tryBreakBlock(ServerLevel level, BlockPos checkPos, boolean foundTarget, ShadowedBlocksConsumer data) {
        BlockState state = level.getBlockState(checkPos);
        Block block = state.getBlock();

        // Если блок уже в списке, пропускаем
        if (data.shadowed.containsKey(checkPos)) return false;

        // Разрешаем нацеливаться на факелы, фонари и костры
        if (block instanceof BaseTorchBlock ||
                block instanceof WallTorchBlock ||
                block instanceof LanternBlock ||
                block instanceof BaseFireBlock ||
                (block instanceof CampfireBlock && state.getValue(CampfireBlock.LIT))) {

            data.shadowed.put(checkPos, new ShadowedBlocksConsumer.ShadowedBlock(block));
            return true;
        }

        return false;
    }
}
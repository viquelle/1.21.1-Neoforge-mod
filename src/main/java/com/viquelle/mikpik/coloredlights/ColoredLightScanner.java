package com.viquelle.mikpik.coloredlights;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ColoredLightScanner {

    private static final int SCAN_RADIUS = 32;

    // 0.25ms
    private static final long TIME_BUDGET_NS = 250_000L;

    // persistent chunk cache
    private static final Map<Long, List<ActiveLight>>
            CHUNK_LIGHT_CACHE = new Long2ObjectOpenHashMap<>();

    // incremental traversal state
    private static int currentDX;
    private static int currentDZ;

    private static int currentSection;
    private static int currentX;
    private static int currentY;
    private static int currentZ;

    private static boolean initialized = false;

    public static void tick(ClientLevel level, LocalPlayer player) {

        long startTime = System.nanoTime();

        if (!initialized) {
            resetScan();
            initialized = true;
        }

        BlockPos playerPos = player.blockPosition();

        int chunkRadius = (SCAN_RADIUS >> 4) + 1;

        int radiusSqr = SCAN_RADIUS * SCAN_RADIUS;

        int baseCX = playerPos.getX() >> 4;
        int baseCZ = playerPos.getZ() >> 4;

        while (System.nanoTime() - startTime < TIME_BUDGET_NS) {

            if (currentDX > chunkRadius) {
                resetScan();
                break;
            }

            int cx = baseCX + currentDX;
            int cz = baseCZ + currentDZ;

            LevelChunk chunk = level.getChunkSource()
                    .getChunk(cx, cz, ChunkStatus.FULL, false);

            if (chunk != null) {
                scanChunkIncremental(
                        chunk,
                        playerPos,
                        radiusSqr
                );
            }

            advanceChunk(chunkRadius);
        }

        buildVisibleLightBuffer(playerPos, radiusSqr);
    }

    private static void scanChunkIncremental(
            LevelChunk chunk,
            BlockPos playerPos,
            int radiusSqr
    ) {

        long chunkKey = chunk.getPos().toLong();

        List<ActiveLight> lights =
                CHUNK_LIGHT_CACHE.computeIfAbsent(
                        chunkKey,
                        k -> new ArrayList<>()
                );

        LevelChunkSection[] sections = chunk.getSections();

        while (currentSection < sections.length) {

            LevelChunkSection section =
                    sections[currentSection];

            if (section == null || section.hasOnlyAir()) {
                currentSection++;
                continue;
            }

            ChunkPos cp = chunk.getPos();

            int baseX = cp.getMinBlockX();
            int baseZ = cp.getMinBlockZ();

            int baseY =
                    chunk.getSectionYFromSectionIndex(currentSection) << 4;

            while (currentX < 16) {

                while (currentY < 16) {

                    while (currentZ < 16) {

                        BlockState state =
                                section.getBlockState(
                                        currentX,
                                        currentY,
                                        currentZ
                                );

                        HardcodedLights.LightData data =
                                HardcodedLights.get(state.getBlock());

                        if (data != null) {

                            int wx = baseX + currentX;
                            int wy = baseY + currentY;
                            int wz = baseZ + currentZ;

                            double dx =
                                    wx + 0.5 - playerPos.getX();

                            double dy =
                                    wy + 0.5 - playerPos.getY();

                            double dz =
                                    wz + 0.5 - playerPos.getZ();

                            double dist2 =
                                    dx * dx + dy * dy + dz * dz;

                            if (dist2 <= radiusSqr) {

                                lights.add(new ActiveLight(
                                        wx + 0.5,
                                        wy + 0.5,
                                        wz + 0.5,
                                        data.radius,
                                        ((data.color >> 16) & 255) * (1f / 255f),
                                        ((data.color >> 8) & 255) * (1f / 255f),
                                        (data.color & 255) * (1f / 255f),
                                        data.intensity
                                ));
                            }
                        }

                        currentZ++;
                    }

                    currentZ = 0;
                    currentY++;
                }

                currentY = 0;
                currentX++;
            }

            currentX = 0;
            currentSection++;
        }

        currentSection = 0;
    }

    private static void buildVisibleLightBuffer(
            BlockPos playerPos,
            int radiusSqr
    ) {

        ColoredLightBuffer.clear();

        for (List<ActiveLight> lights : CHUNK_LIGHT_CACHE.values()) {

            for (ActiveLight light : lights) {

                double dx = light.x() - playerPos.getX();
                double dy = light.y() - playerPos.getY();
                double dz = light.z() - playerPos.getZ();

                double dist2 =
                        dx * dx + dy * dy + dz * dz;

                if (dist2 <= radiusSqr) {

                    ColoredLightBuffer.add(light);

                    if (ColoredLightBuffer.size()
                            >= ColoredLightBuffer.MAX_LIGHTS) {
                        return;
                    }
                }
            }
        }
    }

    private static void advanceChunk(int chunkRadius) {

        currentDZ++;

        if (currentDZ > chunkRadius) {
            currentDZ = -chunkRadius;
            currentDX++;
        }
    }

    private static void resetScan() {

        int radius = (SCAN_RADIUS >> 4) + 1;

        currentDX = -radius;
        currentDZ = -radius;

        currentSection = 0;

        currentX = 0;
        currentY = 0;
        currentZ = 0;
    }
}
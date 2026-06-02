package com.viquelle.mikpik.coloredlights;

import com.viquelle.mikpik.MikpikMod;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ColoredLightScanner {

    private static final int SCAN_RADIUS = 160;
    private static final double RADIUS_SQR = SCAN_RADIUS * SCAN_RADIUS;

    // бонус старым источникам
    private static final double STABILITY_FACTOR = 0.8;

    private ColoredLightScanner() {}

    // ==================== CACHE ====================

    public static void onChunkLoaded(LevelChunk chunk) {
        long chunkKey = chunk.getPos().toLong();

        var chunkLights = new Long2ObjectOpenHashMap<ActiveLight>();

        scanChunk(chunk, chunkLights);

        if (!chunkLights.isEmpty()) {
            ColoredLightCache.CHUNK_LIGHTS.put(chunkKey, chunkLights);

            MikpikMod.LOGGER.debug(
                    "Chunk loaded: {} lights added",
                    chunkLights.size()
            );
        }
    }

    public static void onChunkUnloaded(ChunkPos pos) {
        var removed = ColoredLightCache.CHUNK_LIGHTS.remove(pos.toLong());

        if (removed != null) {
            MikpikMod.LOGGER.debug(
                    "Chunk unloaded: {} lights removed",
                    removed.size()
            );
        }
    }

    public static void onBlockChanged(
            BlockPos pos,
            BlockState oldState,
            BlockState newState
    ) {
        var oldLight = HardcodedLights.get(oldState.getBlock());
        var newLight = HardcodedLights.get(newState.getBlock());

        if (oldLight == null && newLight == null) {
            return;
        }

        long chunkKey = ChunkPos.asLong(
                pos.getX() >> 4,
                pos.getZ() >> 4
        );

        var chunkLights = ColoredLightCache.CHUNK_LIGHTS.get(chunkKey);

        if (chunkLights == null && newLight != null) {
            chunkLights = new Long2ObjectOpenHashMap<>();
            ColoredLightCache.CHUNK_LIGHTS.put(chunkKey, chunkLights);
        }

        if (chunkLights != null) {
            long blockKey = pos.asLong();

            if (oldLight != null) {
                chunkLights.remove(blockKey);
            }

            if (newLight != null) {
                chunkLights.put(
                        blockKey,
                        createLight(pos, newLight)
                );
            }

            if (chunkLights.isEmpty()) {
                ColoredLightCache.CHUNK_LIGHTS.remove(chunkKey);
            }
        }
    }

    // ==================== VISIBLE BUFFER ====================

    public static void buildVisibleLightBuffer(LocalPlayer player) {
        ColoredLightBuffer.clear();

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        int chunkRadius = (SCAN_RADIUS >> 4) + 1;

        int baseCX = ((int) px) >> 4;
        int baseCZ = ((int) pz) >> 4;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {

                long chunkKey = ChunkPos.asLong(
                        baseCX + dx,
                        baseCZ + dz
                );

                var chunkLights = ColoredLightCache.CHUNK_LIGHTS.get(chunkKey);

                if (chunkLights == null) {
                    continue;
                }

                for (var light : chunkLights.values()) {

                    double distSq = light.distanceSq(px, py, pz);

                    if (distSq > RADIUS_SQR) {
                        continue;
                    }

                    // hysteresis
                    if (ColoredLightBuffer.wasVisibleLastFrame(light.id())) {
                        distSq *= STABILITY_FACTOR;
                    }

                    ColoredLightBuffer.addWithDistance(light, distSq);
                }
            }
        }

        ColoredLightBuffer.sortAndTrim();
    }

    // ==================== HELPERS ====================

    private static void scanChunk(
            LevelChunk chunk,
            Long2ObjectOpenHashMap<ActiveLight> out
    ) {
        var sections = chunk.getSections();

        var cp = chunk.getPos();

        int baseX = cp.getMinBlockX();
        int baseZ = cp.getMinBlockZ();

        for (int i = 0; i < sections.length; i++) {

            var section = sections[i];

            if (section == null || section.hasOnlyAir()) {
                continue;
            }

            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {

                        var state = section.getBlockState(x, y, z);

                        var data = HardcodedLights.get(state.getBlock());

                        if (data != null) {

                            var pos = new BlockPos(
                                    baseX + x,
                                    baseY + y,
                                    baseZ + z
                            );

                            out.put(
                                    pos.asLong(),
                                    createLight(pos, data)
                            );
                        }
                    }
                }
            }
        }
    }

    private static ActiveLight createLight(
            BlockPos pos,
            HardcodedLights.LightData data
    ) {
        return new ActiveLight(
                pos.asLong(),

                pos.getX() + 0.5f,
                pos.getY() + 0.5f,
                pos.getZ() + 0.5f,

                data.radius,

                ((data.color >> 16) & 255) / 255.0f,
                ((data.color >> 8) & 255) / 255.0f,
                (data.color & 255) / 255.0f,

                data.intensity
        );
    }
}
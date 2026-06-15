package com.viquelle.mikpik.coloredlights;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ColoredLightScanner {

    private static final int SCAN_RADIUS = 160;
    private static final double RADIUS_SQR = SCAN_RADIUS * SCAN_RADIUS;
    private static final double STABILITY_FACTOR = 0.8;

    private static LevelChunk getWorldChunk(long chunkKey) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        return mc.level.getChunkSource().getChunkNow(
                ChunkPos.getX(chunkKey),
                ChunkPos.getZ(chunkKey)
        );
    }

    public static void onChunkLoaded(LevelChunk chunk) {
        long key = chunk.getPos().toLong();

        var map = new Long2ObjectOpenHashMap<ActiveLight>();
        scanChunk(chunk, map);

        ColoredLightCache.CHUNK_LIGHTS.put(key, map);
    }

    public static void onChunkUnloaded(ChunkPos pos) {
        ColoredLightCache.CHUNK_LIGHTS.remove(pos.toLong());
    }

    public static void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState) {

        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);

        LevelChunk worldChunk = getWorldChunk(chunkKey);
        if (worldChunk == null) return;

        var lightChunk = ColoredLightCache.getLightChunk(chunkKey);
        if (lightChunk == null) return;

        update(pos, worldChunk, lightChunk, oldState);
        update(pos, worldChunk, lightChunk, newState);

        for (BlockPos n : getNeighbors(pos)) {

            long nKey = ChunkPos.asLong(n.getX() >> 4, n.getZ() >> 4);

            LevelChunk wc = getWorldChunk(nKey);
            var lc = ColoredLightCache.getLightChunk(nKey);

            if (wc == null || lc == null) continue;

            BlockState ns = wc.getBlockState(n);
            update(n, wc, lc, ns);
        }
    }

    private static void update(BlockPos pos,
                               LevelChunk world,
                               Long2ObjectOpenHashMap<ActiveLight> lights,
                               BlockState state) {

        long key = pos.asLong();
        lights.remove(key);

        HardcodedLights.LightDefinition data = HardcodedLights.get(state.getBlock());
        if (data == null) return;

        if (data.predicate().test(state)) {
            if (isSurface(pos, world, state, data)) {
                lights.put(key, createLight(pos, state, world, data.data()));
            }
        }
    }

    private static boolean isSurface(BlockPos pos,
                                     LevelChunk world,
                                     BlockState state,
                                     HardcodedLights.LightDefinition data) {

        if (!data.data().isRepresentative) {
            return true;
        }

        boolean hasAir = false;
        int same = 0;

        for (BlockPos n : getNeighbors(pos)) {

            BlockState ns = world.getBlockState(n);

            if (ns.isAir()) {
                hasAir = true;
                continue;
            }

            if (ns.getBlock() == state.getBlock()) {
                same++;
            }
        }

        return hasAir || same < 6;
    }

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

                long chunkKey = ChunkPos.asLong(baseCX + dx, baseCZ + dz);

                var chunk = ColoredLightCache.getLightChunk(chunkKey);
                if (chunk == null) continue;

                for (var light : chunk.values()) {

                    double distSq = light.distanceSq(px, py, pz);

                    if (distSq > RADIUS_SQR) continue;

                    if (ColoredLightBuffer.wasVisibleLastFrame(light.id())) {
                        distSq *= STABILITY_FACTOR;
                    }

                    ColoredLightBuffer.addWithDistance(light, distSq);
                }
            }
        }

        ColoredLightBuffer.upload();
    }

    private static void scanChunk(LevelChunk chunk,
                                  Long2ObjectOpenHashMap<ActiveLight> out) {

        var sections = chunk.getSections();
        var cp = chunk.getPos();

        int baseX = cp.getMinBlockX();
        int baseZ = cp.getMinBlockZ();

        for (int i = 0; i < sections.length; i++) {

            var section = sections[i];
            if (section == null || section.hasOnlyAir()) continue;

            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {

                        var state = section.getBlockState(x, y, z);
                        var data = HardcodedLights.get(state.getBlock());

                        if (data == null) continue;
                        if (!data.predicate().test(state)) continue;
                        var pos = new BlockPos(baseX + x, baseY + y, baseZ + z);

                        if (isSurface(pos, chunk, state, data)) {
                            out.put(pos.asLong(), createLight(pos, state, chunk, data.data()));
                        }
                    }
                }
            }
        }
    }

    private static BlockPos[] getNeighbors(BlockPos p) {
        return new BlockPos[] {
                p.offset(1, 0, 0),
                p.offset(-1, 0, 0),
                p.offset(0, 1, 0),
                p.offset(0, -1, 0),
                p.offset(0, 0, 1),
                p.offset(0, 0, -1)
        };
    }

    private static ActiveLight createLight(BlockPos pos,
                                           BlockState state,
                                           BlockGetter level,
                                           HardcodedLights.LightData data) {

        return new ActiveLight(
                pos.asLong(),
                pos.getX() + 0.5f,
                pos.getY() + 0.5f,
                pos.getZ() + 0.5f,
                state.getLightEmission(level, pos) + 1,
                ((data.color >> 16) & 255) / 255f,
                ((data.color >> 8) & 255) / 255f,
                (data.color & 255) / 255f,
                data.intensity
        );
    }
}
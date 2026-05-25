package com.viquelle.mikpik.coloredlights;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class ColoredLightScanner {
    private static final int SCAN_RADIUS = 32;

    public static void scan(ClientLevel level, LocalPlayer player) {

        ColoredLightBuffer.clear();

        BlockPos playerPos = player.blockPosition();

        int radiusSqr = SCAN_RADIUS * SCAN_RADIUS;

        int chunkRadius = (SCAN_RADIUS >> 4) + 1;

        int baseCX = playerPos.getX() >> 4;
        int baseCZ = playerPos.getZ() >> 4;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {

                int cx = baseCX + dx;
                int cz = baseCZ + dz;

                LevelChunk chunk = level.getChunkSource()
                        .getChunk(cx, cz, ChunkStatus.FULL, false);

                if (chunk == null) continue;

                scanChunk(chunk, playerPos, radiusSqr);
            }
        }
    }

    private static void scanChunk(
            LevelChunk chunk,
            BlockPos playerPos,
            int radiusSqr
    ) {

        LevelChunkSection[] sections = chunk.getSections();

        for (int i = 0; i < sections.length; i++) {

            LevelChunkSection section = sections[i];

            if (section == null || section.hasOnlyAir()) continue;

            scanSection(section, chunk, i, playerPos, radiusSqr);
        }
    }

    private static void scanSection(
            LevelChunkSection section,
            LevelChunk chunk,
            int sectionIndex,
            BlockPos playerPos,
            int radiusSqr
    ) {

        ChunkPos cp = chunk.getPos();

        int baseX = cp.getMinBlockX();
        int baseZ = cp.getMinBlockZ();
        int baseY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {

                    BlockState state = section.getBlockState(x, y, z);

                    HardcodedLights.LightData data =
                            HardcodedLights.get(state.getBlock());

                    if (data == null) continue;

                    int wx = baseX + x;
                    int wy = baseY + y;
                    int wz = baseZ + z;

                    double dx = wx + 0.5 - playerPos.getX();
                    double dy = wy + 0.5 - playerPos.getY();
                    double dz = wz + 0.5 - playerPos.getZ();

                    double dist2 = dx * dx + dy * dy + dz * dz;

                    if (dist2 > radiusSqr) continue;

                    float[] rgb = unpack(data.color);

                    ColoredLightBuffer.add(new ActiveLight(
                            wx + 0.5,
                            wy + 0.5,
                            wz + 0.5,
                            data.radius,
                            rgb[0],
                            rgb[1],
                            rgb[2],
                            data.intensity
                    ));
                }
            }
        }
    }

    private static float[] unpack(int hex) {

        float r = ((hex >> 16) & 255) / 255f;
        float g = ((hex >> 8) & 255) / 255f;
        float b = (hex & 255) / 255f;

        return new float[] { r, g, b };
    }
}
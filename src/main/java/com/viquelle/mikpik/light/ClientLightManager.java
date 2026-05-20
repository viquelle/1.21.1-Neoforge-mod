package com.viquelle.mikpik.light;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.darknesscomputer.Darkness;
import com.viquelle.mikpik.light.source.LightSource;
import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ClientLightManager {
    private static final List<LightSource> SOURCES = new ArrayList<>();

    public static void register(LightSource source) {
        SOURCES.add(source);
    }

    public static void tick(Level level, Player player, float partialTick) {
        for (LightSource source : SOURCES) {
            source.tick(level, partialTick);
        }
    }

    public static void clear() {
        SOURCES.forEach(LightSource::destroy);
    }

    public static float sampleLight(Vec3 pos) {
        float result = 0f;

        for (LightSource source : SOURCES) {
            for (LightHandle<?> handle : source.getLights()) {

                if (!handle.countsAsLight) continue;

                Vec3 lightPos = handle.getPosition();
                float brightness = handle.getBrightness();

                double dx = pos.x - lightPos.x;
                double dy = pos.y - lightPos.y;
                double dz = pos.z - lightPos.z;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < 0.01) {
                    result = Math.max(brightness,result);
                    continue;
                }

                if (handle instanceof PointLightHandle point) {
                    double radius = point.getRadius();
                    if (distSq > radius * radius) continue;

                    float dist = (float)Math.sqrt(distSq);
                    float t = 1.0f - (dist / (float)radius);

                    float influence = brightness * (float) Math.pow(t,2.0f);
                    result = Math.max(influence,result);
                    continue;
                }

                if (handle instanceof AreaLightHandle area) {
                    double range = area.getRange();
                    if (distSq > range * range) continue;

                    Vec3 dir = area.getForward();
                    float invDist = (float) Mth.fastInvSqrt(distSq);
                    float dist = (float)(distSq * invDist);
                    float dot = (float)(
                            dir.x * dx * invDist +
                                    dir.y * dy * invDist +
                                    dir.z * dz * invDist
                    );

                    float halfAngleCos = (float)Math.cos(area.getAngle() * 0.5f);

                    if (dot < halfAngleCos) continue;

                    float angleFactor = (dot - halfAngleCos) / (1.0f - halfAngleCos);
                    angleFactor = Math.max(0f, Math.min(1f, angleFactor));

                    float distanceFactor = 1.0f - (dist / (float)range);

                    float influence = brightness * (float) Math.pow(distanceFactor, 2.0f) * angleFactor;
                    result = Math.max(result, influence);
                }
            }
        }

        return result;
    }

    public static boolean isDarkOnPos(Vec3 pos, Level level, float partialTick) {
        float veilLight = sampleLight(pos) * SanityConstants.VEIL_NORMALIZATION;
        float blockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(pos));
        float skyLight = Darkness.posSkyLight(pos, level, partialTick);
        float localBrightness = Math.max(Math.max(blockLight,skyLight), veilLight);
        return localBrightness <= SanityConstants.BRIGHTNESS_THRESHOLD;
    }
}
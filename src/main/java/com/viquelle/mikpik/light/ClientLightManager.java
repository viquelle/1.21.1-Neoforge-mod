package com.viquelle.mikpik.light;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.light.source.LightSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
                MikpikMod.LOGGER.info("{} {}", lightPos, brightness);
                // =========================
                // POINT LIGHT
                // =========================
                if (handle instanceof PointLightHandle point) {

                    double dx = pos.x - lightPos.x;
                    double dy = pos.y - lightPos.y;
                    double dz = pos.z - lightPos.z;

                    double distSq = dx * dx + dy * dy + dz * dz;
                    double radius = point.getRadius();
                    double radiusSq = radius * radius;

                    if (distSq > radiusSq) continue;

                    float dist = (float)Math.sqrt(distSq);
                    float t = 1.0f - (dist / (float)radius);

                    float influence = brightness * t;
                    if (influence > result) {
                        result = influence;
                    }

                    continue;
                }

                // =========================
                // AREA / SPOT LIGHT
                // =========================
                if (handle instanceof AreaLightHandle area) {

                    double dx = pos.x - lightPos.x;
                    double dy = pos.y - lightPos.y;
                    double dz = pos.z - lightPos.z;

                    double distSq = dx * dx + dy * dy + dz * dz;
                    double range = area.getRange();

                    if (distSq > range * range) continue;

                    float dist = (float)Math.sqrt(distSq);

                    Vec3 dir = area.getForward();

                    Vec3 toPos = new Vec3(dx / dist, dy / dist, dz / dist);

                    float dot = (float)(
                            dir.x * toPos.x +
                                    dir.y * toPos.y +
                                    dir.z * toPos.z
                    );

                    float halfAngleCos = (float)Math.cos(area.getAngle() * 0.5f);

                    if (dot < halfAngleCos) continue;

                    float angleFactor = (dot - halfAngleCos) / (1.0f - halfAngleCos);
                    angleFactor = Math.max(0f, Math.min(1f, angleFactor));

                    float distanceFactor = 1.0f - (dist / (float)range);

                    float influence = brightness * distanceFactor * angleFactor;

                    result = Math.max(result, influence);
                }
                    continue;
            }
        }

        return result;
    }
}
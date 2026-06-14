package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.item.Magnetlampe;
import com.viquelle.mikpik.light.AreaLightHandle;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.*;

public class MagnetlampeLightSource implements LightSource {

    private final Map<String, LightPair> lights = new HashMap<>();

    private static class LightPair {
        PointLightHandle point;
        AreaLightHandle area;
    }

    private static final float RADIUS = 22f;
    private static final float BASE_BRIGHTNESS = 1.0f;

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;

        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        Set<String> validKeys = new HashSet<>();

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;

            float charge = getLampCharge(player);

            if (charge <= 0.0f) continue;

            String key = "magnet_" + player.getUUID();
            validKeys.add(key);

            Vec3 pos = player.getEyePosition(partialTick);

            LightPair pair = lights.computeIfAbsent(key, k -> {
                LightPair p = new LightPair();

                p.point = new PointLightHandle(
                        RADIUS,
                        BASE_BRIGHTNESS * 0.75f,
                        0xFFF2C2,
                        false
                );
                p.point.register();

                p.area = new AreaLightHandle(
                        (float) Math.toRadians(75),
                        RADIUS,
                        BASE_BRIGHTNESS,
                        0xFFF4C2,
                        true,
                        new Vector2f(0.0f, 0.0f)
                );
                p.area.register();

                return p;
            });

            pair.point.setPosition(pos);
            pair.point.setBrightness(BASE_BRIGHTNESS * charge);
            pair.point.setRadius(3.5f * charge * 0.6f + 3.5f * 0.4f);

            pair.area.setPosition(pos);
            pair.area.setBrightness(BASE_BRIGHTNESS * charge);
            pair.area.setRange(RADIUS * charge * 0.6f + RADIUS * 0.4f);
            pair.area.setOrientationFromPlayer(player, partialTick);

            float flicker = 0.98f + (float) Math.random() * 0.04f;

            if (charge < 0.1f) {
                flicker *= (0.96f + (float) Math.random() * 0.08f);
            }

            pair.point.setBrightness(pair.point.getBrightness() * flicker);
            pair.area.setBrightness(pair.point.getBrightness() * flicker);
        }

        lights.keySet().removeIf(key -> {
            if (!validKeys.contains(key)) {
                LightPair p = lights.get(key);
                if (p != null) {
                    if (p.point != null) p.point.unregister();
                    if (p.area != null) p.area.unregister();
                }
                return true;
            }
            return false;
        });
    }

    private float getLampCharge(Player player) {
        float main = Magnetlampe.getPercent(player.getMainHandItem(), player.level());
        float off = Magnetlampe.getPercent(player.getOffhandItem(), player.level());
        return Math.max(main, off);
    }

    @Override
    public void destroy() {
        lights.values().forEach(p -> {
            if (p.point != null) p.point.unregister();
            if (p.area != null) p.area.unregister();
        });
        lights.clear();
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        List<LightHandle> all = new ArrayList<>();
        for (LightPair p : lights.values()) {
            all.add(p.point);
            all.add(p.area);
        }
        return all;
    }
}
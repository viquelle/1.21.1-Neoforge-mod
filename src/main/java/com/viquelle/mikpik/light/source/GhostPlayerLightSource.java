package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class GhostPlayerLightSource implements LightSource {
    private final Map<String, PointLightHandle> lights = new HashMap<>();

    private static final float RADIUS = 5f;
    private static final float BASE_BRIGHTNESS = 1.0f;
    private static final int COLOR = 0xFFF4A5;

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;

        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        Set<String> validKeys = new HashSet<>();

        for (Player player : level.players()) {
            if (!player.isAlive() || !GhostManager.isGhost(player)) continue;

            String key = "ghostplayer_" + player.getUUID();
            validKeys.add(key);
            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle p = new PointLightHandle(
                        RADIUS,
                        BASE_BRIGHTNESS,
                        COLOR,
                        false,
                        true
                );
                p.register();
                return p;
            });

            Vec3 pos = player.getPosition(partialTick);
            light.setPosition(pos.add(0,player.getBoundingBox().getYsize() / 2f,0));
        }

        lights.keySet().removeIf(key -> {
            if (!validKeys.contains(key)) {
                PointLightHandle p = lights.get(key);
                if (p != null) {
                    p.unregister();
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void destroy() {
        lights.values().forEach(LightHandle::unregister);
        lights.clear();
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        return lights.values();
    }
}
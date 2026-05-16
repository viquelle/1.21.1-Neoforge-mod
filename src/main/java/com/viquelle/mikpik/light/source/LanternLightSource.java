package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LanternLightSource implements LightSource {
    private final Map<String, PointLightHandle> lights = new ConcurrentHashMap<>();

    private static final int COLOR = 0xFFD59E;
    private static final boolean OCCLUDED = true;
    private static final float RADIUS = 11.0f;

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        Set<String> validKeys = new HashSet<>();

        // Игроки, держащие фонарь в руках
        for (Player player : level.players()) {
            if (!player.isAlive()) continue;
            boolean inHand = player.getMainHandItem().is(Items.LANTERN)
                    || player.getOffhandItem().is(Items.LANTERN);
            if (!inHand) continue;

            String key = "player_" + player.getUUID();
            validKeys.add(key);

            boolean inWater = player.isUnderWater();
            boolean active = !inWater; // Тухнет ТОЛЬКО в воде, дождь не влияет

            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle h = new PointLightHandle(RADIUS, active ? 1.0f : 0.0f, COLOR, OCCLUDED);
                h.register();
                return h;
            });
            light.setPosition(player.getPosition(partialTick).add(0, 1.2, 0));
            light.setRadius(RADIUS);
            light.setBrightness(active ? 1.0f : 0.0f);
        }

        // Выброшенные фонари вокруг игрока
        var searchBox = localPlayer.getBoundingBox().inflate(32.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            if (!item.getItem().is(Items.LANTERN) || !item.isAlive()) continue;

            String key = "item_" + item.getId();
            validKeys.add(key);

            boolean inWater = item.isUnderWater();
            boolean active = !inWater;

            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle h = new PointLightHandle(RADIUS/2, active ? 1.0f : 0.0f, COLOR, false);
                h.register();
                return h;
            });
            light.setPosition(item.getPosition(partialTick).add(0, 0.5, 0));
            light.setRadius(RADIUS);
            light.setBrightness(active ? 1.0f : 0.0f);
        }

        //Очистка несуществующих источников
        lights.keySet().removeIf(key -> {
            if (!validKeys.contains(key)) {
                PointLightHandle removed = lights.remove(key);
                if (removed != null) removed.unregister();
                return true;
            }
            return false;
        });
    }

    @Override
    public void destroy() {
        lights.values().forEach(PointLightHandle::unregister);
        lights.clear();
    }
}
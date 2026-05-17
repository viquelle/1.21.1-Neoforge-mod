package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LanternLightSource implements LightSource {
    private final Map<String, PointLightHandle> lights = new HashMap<>();

    private static final int COLOR = 0xFFD59E;
    private static final boolean OCCLUDED = true;
    private static final float RADIUS = 11.0f;
    private static final float BRIGHTNESS = 1.0f;

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
            float currentBrightness = active ? BRIGHTNESS : 0f;

            PointLightHandle light = lights.get(key);
            if (light == null) {
                light = new PointLightHandle(RADIUS, currentBrightness, COLOR, OCCLUDED);
                light.register();
                lights.put(key,light);
            }
            light.setPosition(player.getEyePosition(partialTick));
            light.setBrightness(currentBrightness);
        }

        // Выброшенные фонари вокруг игрока
        var searchBox = localPlayer.getBoundingBox().inflate(32.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            if (!item.getItem().is(Items.LANTERN) || !item.isAlive()) continue;

            String key = "item_" + item.getId();
            validKeys.add(key);

            boolean inWater = item.isUnderWater();
            boolean active = !inWater;
            float currentBrightness = active ? BRIGHTNESS : 0f;

            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle h = new PointLightHandle(RADIUS/2, currentBrightness, COLOR, false);
                h.register();
                return h;
            });
            light.setPosition(item.getPosition(partialTick).add(0, 0.5, 0));
            light.setBrightness(currentBrightness);
        }

        //Очистка света от уже несуществующих источников
        lights.keySet().removeIf(key -> {
            if (!validKeys.contains(key)) {
                PointLightHandle removed = lights.get(key);
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

    @Override
    public Collection<? extends LightHandle> getLights() {
        return lights.values();
    }

}
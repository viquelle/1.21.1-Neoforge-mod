package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TorchLightSource implements LightSource {
    private final Map<String, PointLightHandle> lights = new HashMap<>();

    // Тепловато-белый (янтарный) оттенок
    private static final int COLOR = 0xFFD59E;
    private static final boolean OCCLUDED = true;
    private static final float RADIUS = 12.0f;
    private static final float BRIGHTNESS = 1.0f;

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        Set<String> validKeys = new HashSet<>();

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;
            boolean inHand = player.getMainHandItem().is(Items.TORCH)
                    || player.getOffhandItem().is(Items.TORCH);
            if (!inHand) continue;

            String key = "player_" + player.getUUID();
            validKeys.add(key);

            BlockPos pos = player.blockPosition();
            boolean inWater = player.isUnderWater();
            boolean inRain = level.isRaining() && level.isRainingAt(pos);
            boolean active = !inWater && !inRain; // Тухнет под дождем И в воде
            float currentBrightness = active ? BRIGHTNESS : 0f;

            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle h = new PointLightHandle(RADIUS, currentBrightness, COLOR, OCCLUDED);
                h.register();
                return h;
            });
            light.setPosition(player.getEyePosition(partialTick));
            light.setBrightness(currentBrightness);
        }

        var searchBox = localPlayer.getBoundingBox().inflate(32.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            if (!item.getItem().is(Items.TORCH) || !item.isAlive()) continue;

            String key = "item_" + item.getId();
            validKeys.add(key);

            BlockPos pos = item.blockPosition();
            boolean inWater = item.isUnderWater();
            boolean inRain = level.isRaining() && level.isRainingAt(pos);
            boolean active = !inWater && !inRain;
            float currentBrightness = active ? BRIGHTNESS : 0f;

            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle h = new PointLightHandle(RADIUS/2, currentBrightness, COLOR, false);
                h.register();
                return h;
            });
            light.setPosition(item.getEyePosition(partialTick).add(0.0f,0.5f,0.0f));
            light.setBrightness(currentBrightness);
        }

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
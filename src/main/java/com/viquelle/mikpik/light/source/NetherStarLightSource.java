package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.*;

public class NetherStarLightSource implements LightSource { // ЧИСТО ТЕСТОВОЕ, пусть будет, но это тестовый класс
    private final Map<String, PointLightHandle> lights = new HashMap<>();

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        // Ключи, которые должны остаться живыми в этом тике
        Set<String> validKeys = new HashSet<>();

        // се игроки с звездой
        for (Player player : level.players()) {
            boolean hasStar = player.getInventory().contains(Items.NETHER_STAR.getDefaultInstance());
            String key = "player_" + player.getUUID();

            if (hasStar && player.isAlive()) {
                validKeys.add(key);
                boolean inHand = player.getMainHandItem().is(Items.NETHER_STAR)
                        || player.getOffhandItem().is(Items.NETHER_STAR);
                float currBrightness = inHand ? 1.5f : 1.0f;
                float currRadius = inHand ? 8.0f : 4.0f;
                PointLightHandle light = lights.computeIfAbsent(key, k -> {
                    PointLightHandle h = new PointLightHandle(
                            currRadius,
                            currBrightness,
                            0xFFFFFF,
                            true);
                    h.register();
                    return h;
                });
                light.setPosition(player.getEyePosition(partialTick));
                light.setBrightness(currBrightness);
                light.setRadius(currRadius);
            }
        }

        // Выброшенные звезды вокруг локального игрока
        var searchBox = localPlayer.getBoundingBox().inflate(32.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            if (item.getItem().is(Items.NETHER_STAR) && item.isAlive()) {
                String key = "item_" + item.getId();
                validKeys.add(key);

                PointLightHandle light = lights.computeIfAbsent(key, k -> {
                    PointLightHandle h = new PointLightHandle(5.0f, 2.0f, 0xFFFFFF, false);
                    h.register();
                    return h;
                });
                light.setPosition(item.getPosition(partialTick).add(0, 0.1, 0));
            }
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
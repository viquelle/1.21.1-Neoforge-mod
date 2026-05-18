package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LanternLightSource implements LightSource {
    private final Map<String, PointLightHandle> lights = new HashMap<>();

    private enum LanternType {
        LANTERN(Items.LANTERN, 15.0f, 1.5f, 0xFFD59E, true),
        SOUL_LANTERN(Items.SOUL_LANTERN, 12.0f, 1.2f, 0x5CACEE, true);

        Item item;
        float radius;
        float brightness;
        int color;
        boolean occlusion;

        LanternType(Item lantern, float radius, float brightness, int color, boolean occlusion) {
            this.item = lantern;
            this.radius = radius;
            this.brightness = brightness;
            this.color = color;
            this.occlusion = occlusion;
        }

        static LanternType fromItem(Item item) {
            for (LanternType type : values()) {
                if (type.item == item) return type;
            }
            return null;
        }
    }

    private static final int COLOR = 0xFFD59E;
    private static final boolean OCCLUDED = true;
    private static final float RADIUS = 15.0f;
    private static final float BRIGHTNESS = 1.5f;

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        Set<String> validKeys = new HashSet<>();

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;

            LanternType mainHand = LanternType.fromItem(player.getMainHandItem().getItem());
            LanternType offHand = LanternType.fromItem(player.getOffhandItem().getItem());

            if (mainHand == null && offHand == null) continue;

            if (mainHand != null) {
                String key = "player_" + player.getUUID() + "_main";
                validKeys.add(key);
                processLantern(player, mainHand, key, partialTick);
            }

            if (offHand != null) {
                String key = "player_" + player.getUUID() + "_off";
                validKeys.add(key);
                processLantern(player, offHand, key, partialTick);
            }

        }

        // Выброшенные фонари вокруг игрока
        var searchBox = localPlayer.getBoundingBox().inflate(32.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            LanternType type = LanternType.fromItem(item.getItem().getItem());
            if (type == null || !item.isAlive()) continue;

            String key = "item_" + item.getId();
            validKeys.add(key);

            boolean inWater = item.isUnderWater();

            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle h = new PointLightHandle(type.radius, type.brightness, type.color, false);
                h.register();
                return h;
            });
            light.setPosition(item.getPosition(partialTick).add(0, 0.5, 0));
            light.setBrightness(!inWater? type.brightness : 0f);
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

    private void processLantern(Player player, LanternType type, String key, float partialTick) {
        boolean inWater = player.isUnderWater();

        PointLightHandle light = lights.computeIfAbsent(key, k -> {
            PointLightHandle h = new PointLightHandle(type.radius, type.brightness, type.color, type.occlusion);
            h.register();
            return h;
        });

        light.setPosition(player.getEyePosition(partialTick));
        light.setBrightness(!inWater ? type.brightness : 0f);
        light.setColor(type.color);
        light.setRadius(type.radius);
    }
}
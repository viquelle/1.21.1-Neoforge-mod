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

public class TorchLightSource implements LightSource {
    private final Map<String, PointLightHandle> lights = new HashMap<>();

    private enum TorchType {
        TORCH(Items.TORCH, 15.0f, 1.0f, 0xFFD294, true),
        SOUL_TORCH(Items.SOUL_TORCH, 10f, 1.2f, 0x3d64FF, true),
        REDSTONE_TORCH(Items.REDSTONE_TORCH, 7f, 1.1f, 0xFF4040, true);

        Item item;
        float radius;
        float brightness;
        int color;
        boolean occlusion;

        TorchType(Item torch, float radius, float brightness, int color, boolean occlusion) {
            this.item = torch;
            this.radius = radius;
            this.brightness = brightness;
            this.color = color;
            this.occlusion = occlusion;
        }

        static TorchType fromItem(Item item) {
            for (TorchType type : values()) {
                if (type.item == item) return type;
            }
            return null;
        }
    }

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        Set<String> validKeys = new HashSet<>();

        for (Player player : level.players()) {
            if (!player.isAlive()) continue;

            TorchType mainHand = TorchType.fromItem(player.getMainHandItem().getItem());
            TorchType offHand = TorchType.fromItem(player.getOffhandItem().getItem());

            if (mainHand == null && offHand == null) continue;

            if (mainHand != null) {
                String key = "player_" + player.getUUID() + "_main";
                validKeys.add(key);
                processTorch(player, mainHand, key, partialTick);
            }

            if (offHand != null) {
                String key = "player_" + player.getUUID() + "_off";
                validKeys.add(key);
                processTorch(player, offHand, key, partialTick);
            }

        }

        var searchBox = localPlayer.getBoundingBox().inflate(32.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, searchBox)) {
            TorchType type = TorchType.fromItem(item.getItem().getItem());
            if (type == null || !item.isAlive()) continue;

            String key = "item_" + item.getId();
            validKeys.add(key);

            Vec3 pos = item.getPosition(partialTick);
            boolean inWater = item.isUnderWater();
            boolean inRain = level.isRaining() && level.isRainingAt(BlockPos.containing(pos));
            boolean active = !inWater && !inRain;

            PointLightHandle light = lights.computeIfAbsent(key, k -> {
                PointLightHandle h = new PointLightHandle(type.radius, type.brightness, type.color, false);
                h.register();
                return h;
            });
            float depthCoeff = (float) Math.clamp((64f - pos.y)/64f, 0.0f, 1.0f);
            float r = type.radius;
            float b = type.brightness;
            r = (float) (r - r * depthCoeff * 0.5);
            b = b - depthCoeff * b;
            light.setPosition(item.getPosition(partialTick).add(0.0f,0.1f,0.0f));
            light.setBrightness(active ? b : 0f);
            light.setRadius(r);
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

    private void processTorch(Player player, TorchType type, String key, float partialTick) {
        Vec3 pos = player.getEyePosition(partialTick);
        boolean inWater = player.isUnderWater();
        boolean inRain = player.level().isRaining() && player.level().isRainingAt(BlockPos.containing(pos));
        boolean active = !inWater && !inRain; // Тухнет под дождем И в воде

        PointLightHandle light = lights.computeIfAbsent(key, k -> {
            PointLightHandle h = new PointLightHandle(type.radius, type.brightness, type.color, type.occlusion);
            h.register();
            return h;
        });

        float depthCoeff = (float) Math.clamp((64f - pos.y)/64f, 0.0f, 1.0f);
        float r = type.radius;
        float b = type.brightness;
        r = (float) (r - r * depthCoeff * 0.5);
        b = b - depthCoeff * b;
        light.setPosition(pos);
        light.setBrightness(active ? b : 0f);
        light.setColor(type.color);
        light.setRadius(r);
    }
}
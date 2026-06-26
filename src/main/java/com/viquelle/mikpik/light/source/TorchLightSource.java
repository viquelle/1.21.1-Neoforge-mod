package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.*;

public class TorchLightSource implements LightSource {
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

    private class TorchState {
        PointLightHandle light;
        float currentBrightness = 0f;
        TorchType type;
        Entity owner;
        boolean shouldRemove = false; // Если яркость < 0.001, то удалит
        boolean isDead = false;

        TorchState(Entity owner, TorchType type, boolean instantBright) {
            this.owner = owner;
            this.type = type;
            currentBrightness = instantBright ? type.brightness : currentBrightness;
            this.light = createLight(type);
        }

        void update() {
            MikpikMod.LOGGER.info("trying update {} in {}", type.item, owner.getName());
            MikpikMod.LOGGER.info("{} \n{} \n{} \n{} \n{} \n{}", currentBrightness, type, owner, shouldRemove, currentPartialTick, currentDeltaTime);
            if (owner.isRemoved()) shouldRemove = true;

            updatePosition();
            updateBrightness(currentDeltaTime);
            if (shouldRemove && currentBrightness < 0.001) isDead = true;
        }

        private void updatePosition() {
            light.setPosition(owner.getEyePosition(currentPartialTick));
        }

        private void updateBrightness(float deltatime) {
            if (owner.isRemoved()) currentBrightness = Math.max(currentBrightness - SUPER_EXTING_SPEED * deltatime, 0f);
            else {
                if (shouldRemove) {
                    currentBrightness = Math.max(currentBrightness - EXTING_SPEED * deltatime, 0f);
                } else {
                    currentBrightness = Math.min(currentBrightness + LIT_SPEED * deltatime, type.brightness);
                }
            }
            light.setBrightness(currentBrightness);
        }

        private void kill() {
            light.unregister();
        }

        private PointLightHandle createLight(TorchType type) {
            PointLightHandle light = new PointLightHandle(type.radius, currentBrightness, type.color, type.occlusion);
            light.register();
            return light;
        }
    }
    private final Map<String, TorchState> torches = new HashMap<>();
    private final float LIT_SPEED = 0.5f; // 2s
    private final float EXTING_SPEED = 2f; // 0.5f
    private final float SUPER_EXTING_SPEED = 5f; // 0.2f
    private float currentPartialTick;
    private float currentDeltaTime;

    @Override
    public void tick(Level level, float partialTick) {
        if (level == null) return;
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        currentPartialTick = partialTick;
        currentDeltaTime = (level.getGameTime() + partialTick - ClientLightManager.getLastFrameTick()) / 20f;

        Iterator<Map.Entry<String, TorchState>> it = torches.entrySet().iterator();
        while (it.hasNext()) {
            TorchState state = it.next().getValue();
            if (state.isDead) {
                state.kill();
                it.remove();
                continue;
            }
            state.shouldRemove = true;
        }


        for (Player player : level.players()) {
            if (!player.isAlive()) continue;
            ItemStack HandItem = player.getMainHandItem();
            TorchType Torch = TorchType.fromItem(HandItem.getItem());
            if (Torch != null) {
                String key = "player_" + player.getUUID() + "_main_" + HandItem.getItem();
                activateOrFlag(player, Torch, key, false);
            }
            HandItem = player.getOffhandItem();
            Torch = TorchType.fromItem(HandItem.getItem());
            if (Torch != null) {
                String key = "player_" + player.getUUID() + "_off_" + HandItem.getItem();
                activateOrFlag(player, Torch, key, false);
            }
        }


        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, localPlayer.getBoundingBox().inflate(32))) {
            ItemStack itemStack = item.getItem();
            TorchType torch = TorchType.fromItem(itemStack.getItem());
            if (torch != null) {
                String key = "item_" + item.getId() + "_" + itemStack.getItem();
                activateOrFlag(item, torch, key, true);
            }
        }


        for (TorchState state : torches.values()) {
            state.update();
        }
    }

    @Override
    public void destroy() {
        for (TorchState state : torches.values()) {
            state.light.unregister();
        }
        torches.clear();
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        List<PointLightHandle> buffer = new ArrayList<>();

        for (TorchState state : torches.values())
            buffer.add(state.light);

        return buffer;
    }

    private void activateOrFlag(Entity entity, TorchType type, String key, boolean instantBright) {
        TorchState state = torches.get(key);
        if (state == null) {
            state = new TorchState(entity, type, instantBright);
            torches.put(key, state);
        } else {
            state.shouldRemove = false;
        }
    }
}
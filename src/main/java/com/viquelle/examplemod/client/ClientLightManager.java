package com.viquelle.examplemod.client;

import com.viquelle.examplemod.ExampleMod;
import com.viquelle.examplemod.client.light.AbstractLight;
import com.viquelle.examplemod.client.light.AreaLight;
import com.viquelle.examplemod.client.light.PointLight;
import com.viquelle.examplemod.item.AbstractLightItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

import java.util.*;

public class ClientLightManager {
    private static final Map<String, AbstractLight<?>> activeLights = new HashMap<>();
    private static final Map<String, Long> lastUsed = new HashMap<>();
    private static final String AMBIENT_SUFFIX = "_ambient_aura";
    private static long lastFrameTime = System.currentTimeMillis();

    public static void initPlayerAmbientLight(LocalPlayer player) {
        String ambientKey = player.getUUID() + AMBIENT_SUFFIX;
        PointLight light = new PointLight.Builder()
                .setPlayer(player)
                .setBrightness(0f)
                .setColor(0xFFDDAB)
                .build();

        add(ambientKey, light);
    }

    public static void add(String key, AbstractLight<?> light) {
        activeLights.put(key,light);
        light.register();
    }

    public static void remove(String key) {
        AbstractLight<?> light = activeLights.remove(key);
        if (light != null) {
            light.unregister();
        }
    }

    public static AbstractLight<?> getLight(String key) {
        return activeLights.get(key);
    }

    public static void tick(float pT) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;
        long now = System.currentTimeMillis();

        Set<String> currentFrameLights = new HashSet<>();
        currentFrameLights.add(localPlayer.getUUID() + AMBIENT_SUFFIX);
        updateAmbientLight(localPlayer, pT, now);
        ;
        for (Player player: localPlayer.level().players()) {
            if (!checkHand(player, InteractionHand.MAIN_HAND, currentFrameLights)) checkHand(player, InteractionHand.OFF_HAND, currentFrameLights);
        }

        activeLights.keySet().removeIf(key -> {
            if (!currentFrameLights.contains(key)) {
                activeLights.get(key).unregister();
                return true;
            } return false;
        });

        for (AbstractLight<?> light : activeLights.values()) {
            light.tick(pT);
        }
    }

    private static void updateAmbientLight(LocalPlayer player, float pT, long now) {
        String key = player.getUUID() + AMBIENT_SUFFIX;
        PointLight ambient = (PointLight) activeLights.get(key);
        // Safety check: verify it's the right type and exists

        // Prevent garbage collection of this light
        lastUsed.put(key, now);
        float deltaTime = (now - lastFrameTime) / 1000f;
        lastFrameTime = now;
        // A. Check Environment Conditions
        BlockPos pos = player.blockPosition();
        int skyLight = player.level().getBrightness(LightLayer.SKY, pos);
        int blockLight = player.level().getBrightness(LightLayer.BLOCK, pos);

        boolean isDark = skyLight < 2 && blockLight == 0;
        // B. Check if Player has other active lights (e.g. Flashlight)
        // We look for any active light belonging to this player that isn't the ambient light itself
        boolean hasActiveSource = activeLights.entrySet().stream()
                .filter(e -> !e.getKey().equals(key)) // Ignore self
                .filter(e -> e.getKey().contains(player.getUUID().toString())) // Check player ownership
                .anyMatch(e -> e.getValue().brightness > 0.05f); // Is it actually on?

        // C. Calculate Target Brightness
        // If it's dark and we have no other light -> ON (0.3f is subtle). Otherwise -> OFF.
        float targetBrightness = (isDark && !hasActiveSource) ? 0.3f : 0.0f;
        float fadeSpeed = 0.2f;
        ExampleMod.LOGGER.info("data: {} || {}", deltaTime, targetBrightness);
        if (ambient.brightness < targetBrightness) {
            ambient.setBrightness(Math.min(ambient.brightness + deltaTime * fadeSpeed, targetBrightness));
        } else if (ambient.brightness > targetBrightness) {
            ambient.setBrightness(Math.max(ambient.brightness - deltaTime * fadeSpeed, targetBrightness));
        }
    }

    private static boolean checkHand(Player player, InteractionHand hand, Set<String> currentFrameLights) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof AbstractLightItem lightItem) {
            if (AbstractLightItem.isEnabled(stack)) {
                String key = lightItem.getKey(player);
                currentFrameLights.add(key);

                if (!activeLights.containsKey(key)) {
                    AbstractLight<?> light = createLight(lightItem.getSettings(stack), player);
                    add(key, light);
                    return true;
                }
            }
        }
        return false;
    }

    private static AbstractLight<?> createLight(AbstractLightItem.LightSettings settings, Player player) {
        return switch (settings.type()) {
            case POINT -> new PointLight.Builder()
                    .setPlayer(player)
                    .setBrightness(settings.brightness())
                    .setRadius(settings.radius())
                    .setColor(settings.color())
                    .build();
            case AREA -> new AreaLight.Builder()
                    .setPlayer(player)
                    .setBrightness(settings.brightness())
                    .setAngle(settings.angle())
                    .setColor(settings.color())
                    .build();
        };
    }
}

package com.viquelle.examplemod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.viquelle.examplemod.client.light.AbstractLight;
import com.viquelle.examplemod.client.light.AreaLight;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.LightData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class ClientLightManager {
    private static final Map<String, AbstractLight<?>> activeLights = new HashMap<>();

    public static void initPlayerLight(LocalPlayer player) {
        UUID id = player.getUUID();

        if (activeLights.containsKey(id)) return;

        AreaLight light = new AreaLight.Builder()
                .setPlayer(player)
                .setBrightness(0f)
                .setColor(0x000000)
                .setPosition(player.getEyePosition())
                .build();

        VeilRenderSystem.renderThreadExecutor().execute(light::register);

        add("dummy", light);
        remove("dummy");
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

    private static final Map<String, Long> lastUsed = new HashMap<>();
    public static void tick(float pT) {
        long now = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, AbstractLight<?>> entry : activeLights.entrySet()) {
            String key = entry.getKey();
            AbstractLight<?> light = entry.getValue();

            if (light.brightness < 0.01f) {
                lastUsed.putIfAbsent(key,now);
                if (now - lastUsed.get(key) > 10_000) {
                    toRemove.add(key);
                }
            } else {
                lastUsed.put(key, now);
            }

            light.tick(pT);
        }

        for (String key : toRemove) {
            remove(key);
            lastUsed.remove(key);
        }
    }

}

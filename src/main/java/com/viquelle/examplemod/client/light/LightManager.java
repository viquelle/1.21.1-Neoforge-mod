package com.viquelle.examplemod.client.light;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import java.util.ArrayList;
import java.util.List;

public class LightManager {
    private static final List<IPlayerLight> lights = new ArrayList<>();

    public static void register(IPlayerLight light) {
        lights.add(light);
        light.create();
    }

    public static void tickLocalPlayer(float pt) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;

        for (IPlayerLight light : lights) {
            light.tick(p,pt);
        }
    }

    public static void clearAll() {
        lights.forEach(IPlayerLight::destroy);
        lights.clear();
    }
}

package com.viquelle.examplemod.client.light;

import java.util.List;

public class LightManager {
    private static List<AbstractLight<?>> lights;
    public static List<AbstractLight<?>> getLights() {
        return lights;
    }

    public static void tick(float pt) {
        getLights().forEach(light -> light.tick(pt));
    }
}

package com.viquelle.mikpik.coloredlights;

import java.util.ArrayList;
import java.util.List;

public final class ColoredLightBuffer {
    public static final int MAX_LIGHTS = 64;

    private static final List<ActiveLight> LIGHTS = new ArrayList<>(MAX_LIGHTS);

    public static void clear() {
        LIGHTS.clear();
    }

    public static void add(ActiveLight light) {
        if (LIGHTS.size() < MAX_LIGHTS) {
            LIGHTS.add(light);
        }
    }

    public static List<ActiveLight> get() {
        return LIGHTS;
    }

    public static int size() {
        return LIGHTS.size();
    }
}
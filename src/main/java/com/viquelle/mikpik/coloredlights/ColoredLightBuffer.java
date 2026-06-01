package com.viquelle.mikpik.coloredlights;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ColoredLightBuffer {
    public static final int MAX_LIGHTS = 256;

    private static final List<ActiveLight> LIGHTS = new ArrayList<>(MAX_LIGHTS);
    private static final List<LightDistancePair> SORTING_LIST = new ArrayList<>();

    private static final LongOpenHashSet PREVIOUS_LIGHTS = new LongOpenHashSet();
    private static final LongOpenHashSet CURRENT_LIGHTS = new LongOpenHashSet();

    private ColoredLightBuffer() {}

    public static void clear() {
        LIGHTS.clear();
        SORTING_LIST.clear();
    }

    public static boolean wasVisibleLastFrame(long id) {
        return PREVIOUS_LIGHTS.contains(id);
    }

    public static void addWithDistance(ActiveLight light, double distanceSq) {
        SORTING_LIST.add(new LightDistancePair(light, distanceSq));
    }

    public static void sortAndTrim() {
        SORTING_LIST.sort(Comparator.comparingDouble(LightDistancePair::distanceSq));

        int count = Math.min(SORTING_LIST.size(), MAX_LIGHTS);

        LIGHTS.clear();
        CURRENT_LIGHTS.clear();

        for (int i = 0; i < count; i++) {
            ActiveLight light = SORTING_LIST.get(i).light();

            LIGHTS.add(light);
            CURRENT_LIGHTS.add(light.id());
        }

        PREVIOUS_LIGHTS.clear();
        PREVIOUS_LIGHTS.addAll(CURRENT_LIGHTS);
    }

    public static List<ActiveLight> get() {
        return LIGHTS;
    }

    public static int size() {
        return LIGHTS.size();
    }

    private record LightDistancePair(
            ActiveLight light,
            double distanceSq
    ) {}
}
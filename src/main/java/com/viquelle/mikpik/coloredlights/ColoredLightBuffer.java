package com.viquelle.mikpik.coloredlights;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ColoredLightBuffer {
    public static final int MAX_LIGHTS = 64;

    private static final List<ActiveLight> LIGHTS = new ArrayList<>(MAX_LIGHTS);
    private static final List<LightDistancePair> SORTING_LIST = new ArrayList<>();

    public static void clear() {
        LIGHTS.clear();
        SORTING_LIST.clear();
    }

    public static void addWithDistance(ActiveLight light, double distanceSq) {
        if (SORTING_LIST.size() < MAX_LIGHTS * 2) { // Буфер для сортировки
            SORTING_LIST.add(new LightDistancePair(light, distanceSq));
        }
    }

    public static void sortAndTrim() {
        // Сортируем по расстоянию (ближайшие первые)
        SORTING_LIST.sort(Comparator.comparingDouble(LightDistancePair::distanceSq));

        // Берем только MAX_LIGHTS ближайших
        int count = Math.min(SORTING_LIST.size(), MAX_LIGHTS);
        LIGHTS.clear();
        for (int i = 0; i < count; i++) {
            LIGHTS.add(SORTING_LIST.get(i).light());
        }
    }

    public static List<ActiveLight> get() {
        return LIGHTS;
    }

    public static int size() {
        return LIGHTS.size();
    }

    private record LightDistancePair(ActiveLight light, double distanceSq) {}
}
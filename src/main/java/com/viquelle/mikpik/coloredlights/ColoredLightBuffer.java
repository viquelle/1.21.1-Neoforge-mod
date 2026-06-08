package com.viquelle.mikpik.coloredlights;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.ArrayList;
import java.util.List;

public final class ColoredLightBuffer {

    public static final int MAX_LIGHTS = 256;

    private static final List<ActiveLight> LIGHTS = new ArrayList<>(MAX_LIGHTS);

    private static final ActiveLight[] LIGHTS_BUF = new ActiveLight[MAX_LIGHTS];
    private static final double[] DIST_BUF = new double[MAX_LIGHTS];

    private static int size = 0;

    private static final LongOpenHashSet PREVIOUS_LIGHTS = new LongOpenHashSet();
    private static final LongOpenHashSet CURRENT_LIGHTS = new LongOpenHashSet();

    private ColoredLightBuffer() {}

    public static void clear() {
        LIGHTS.clear();
        size = 0;
    }

    public static void frame() {
        PREVIOUS_LIGHTS.clear();
        PREVIOUS_LIGHTS.addAll(CURRENT_LIGHTS);
        CURRENT_LIGHTS.clear();
    }

    public static boolean wasVisibleLastFrame(long id) {
        return PREVIOUS_LIGHTS.contains(id);
    }

    public static void addWithDistance(ActiveLight light, double distSq) {

        if (size < MAX_LIGHTS) {
            LIGHTS_BUF[size] = light;
            DIST_BUF[size] = distSq;
            CURRENT_LIGHTS.add(light.id());
            size++;
            return;
        }

        int worst = 0;
        double worstDist = DIST_BUF[0];

        for (int i = 1; i < MAX_LIGHTS; i++) {
            if (DIST_BUF[i] > worstDist) {
                worstDist = DIST_BUF[i];
                worst = i;
            }
        }

        if (distSq >= worstDist) {
            return;
        }

        ActiveLight old = LIGHTS_BUF[worst];
        CURRENT_LIGHTS.remove(old.id());

        LIGHTS_BUF[worst] = light;
        DIST_BUF[worst] = distSq;
        CURRENT_LIGHTS.add(light.id());
    }

    public static void sortAndTrim() {
        LIGHTS.clear();

        for (int i = 0; i < size; i++) {
            LIGHTS.add(LIGHTS_BUF[i]);
        }
    }

    public static List<ActiveLight> get() {
        return LIGHTS;
    }

    public static int size() {
        return size;
    }
}
package com.viquelle.examplemod.client.sanity;

public class SanityData {
    private static float sanity = 100.0f;
    private static float maxSanity = 100.0f;

    public static void update(float s) {
        sanity = s;
    }

    public static float getSanity() { return sanity; }

    public static void setSanity(float value) {
        sanity = Math.max(0, Math.min(100f, value));
    }
}

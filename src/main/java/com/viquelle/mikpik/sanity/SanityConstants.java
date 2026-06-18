package com.viquelle.mikpik.sanity;

public class SanityConstants {
    public static final float MIN_SANITY = 0.0f;
    public static final float MAX_SANITY = 100.0f;
    public static final float VEIL_NORMALIZATION = 10f; // 15radius / 1.5light = default torch

    // значения ЗА ТИК
    // 0.01 * 20 * 60 = 12s/m
    public static final float DARK_DRAIN_PER_TICK = -0.05f;
    public static final float BRIGHTNESS_THRESHOLD = 1f;
    public static final float SKY_DAY_REGEN_PER_TICK = 0.008f;
    public static final float SHELTER_REGEN_PER_TICK = 0.015f;
    public static final float NEAR_PLAYER_REGEN_PER_TICK = 0.01f;
    public static final float NEAR_GHOSTPLAYER_DRAIN_PER_TICK = -0.02f;

    public static final float FLOWER_CROWN_REGEN_PER_TICK = 0.015f;

}
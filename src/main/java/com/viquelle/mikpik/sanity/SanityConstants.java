package com.viquelle.mikpik.sanity;

public class SanityConstants {
    public static final float MIN_SANITY = 0.0f;
    public static final float MAX_SANITY = 100.0f;

    // значения ЗА СЕКУНДУ
    public static final float DARK_DRAIN_PER_SECOND = -0.4f;
    public static final float SKY_DAY_REGEN_PER_SECOND = 0.4f;
    public static final float SHELTER_REGEN_PER_SECOND = 0.6f;
    public static final float NEAR_PLAYER_REGEN_PER_SECOND = 0.3f;

    public static float perTick(float perSecond) {
        return perSecond / 20.0f;
    }
}
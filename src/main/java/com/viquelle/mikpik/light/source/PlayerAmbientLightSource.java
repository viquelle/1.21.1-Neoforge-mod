package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.darknesscomputer.Darkness;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.util.Collection;
import java.util.List;

public class PlayerAmbientLightSource implements LightSource{
    private static final int COLOR = 0x0B6A99;
    private static final float BASE_BRIGHTNESS = 0.67f;
    private static final float DEEP_ADDITION_BRIGHTNESS = 2f;
    private static final float MAX_RADIUS = 8f;
    private float CURRENT_RADIUS = 0f;
    private float CURRENT_BRIGHTNESS = 0f;;

    private static final float EPSILON = 0.001f;
    private static final float PROGRESS_TIME = 5f; // 5 seconds
    private long lastTime = System.nanoTime();

    private boolean registered = false;
    private final PointLightHandle light = new PointLightHandle(0, BASE_BRIGHTNESS, COLOR, true, false);

    @Override
    public void tick(Level level, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null || !level.isClientSide) return;

        if (!registered) {
            light.register();
            registered = true;
        }

        long now = System.nanoTime();
        float deltaSeconds = (now - lastTime) / 1_000_000_000f;
        lastTime = now;

        boolean isDark = ClientLightManager.isDarkOnPos(
                player.getEyePosition(partialTick),
                level,
                partialTick);

        float targetRadius = isDark ? MAX_RADIUS : 0.0f;

        if (Math.abs(CURRENT_RADIUS - targetRadius) > EPSILON) {
            if (CURRENT_RADIUS < targetRadius) {
                CURRENT_RADIUS = Math.min(CURRENT_RADIUS + (MAX_RADIUS / PROGRESS_TIME) * deltaSeconds, targetRadius);
            } else {
                CURRENT_RADIUS = Math.max(CURRENT_RADIUS - (MAX_RADIUS / PROGRESS_TIME) * deltaSeconds, 0f);
            }
        }

        CURRENT_BRIGHTNESS = BASE_BRIGHTNESS;
//        CURRENT_BRIGHTNESS += Mth.lerp(
//                Math.clamp(
//                        (float) (player.getEyePosition(partialTick).y - 24) / (-64 - 24), 0.0f, 1.0f),
//                0.0f,
//                DEEP_ADDITION_BRIGHTNESS
//        );

        light.setBrightness(CURRENT_BRIGHTNESS);
        light.setRadius(CURRENT_RADIUS);
        light.setPosition(player.getEyePosition(partialTick));
    }

    @Override
    public void destroy() {
        if (registered) {
            light.unregister();
            registered = false;
        }
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        return List.of(light);
    }
}
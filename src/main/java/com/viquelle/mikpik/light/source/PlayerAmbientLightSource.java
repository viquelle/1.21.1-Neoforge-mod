package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.LightHandle;
import com.viquelle.mikpik.light.PointLightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PlayerAmbientLightSource implements LightSource{
    private static final int COLOR = 0x5166B6;
    private static final float BRIGHTNESS = 2.5f;
    private static final float MAX_RADIUS = 5f;
    private float CURRENT_RADIUS = 0f;
    private static final float BRIGHTNESS_THRESHOLD = 2f;

    private static final float EPSILON = 0.001f;
    private static final float PROGRESS_TIME = 5f; // 5 seconds
    private long lastTime = System.nanoTime();

    private boolean registered = false;
    private final PointLightHandle light = new PointLightHandle(0, BRIGHTNESS, COLOR, false, false);

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

        int localBrightness = level.getMaxLocalRawBrightness(player.blockPosition());
        float targetRadius =
                (localBrightness <= BRIGHTNESS_THRESHOLD) && ClientLightManager.sampleLight(player.getEyePosition(partialTick)) < 0.5f ? MAX_RADIUS : 0.0f;

        float progress_speed = MAX_RADIUS / PROGRESS_TIME;
        if (Math.abs(CURRENT_RADIUS - targetRadius) > EPSILON) {
            if (CURRENT_RADIUS < targetRadius) {
                CURRENT_RADIUS = Math.min(CURRENT_RADIUS + progress_speed * deltaSeconds, targetRadius);
            } else {
                CURRENT_RADIUS = Math.max(CURRENT_RADIUS - progress_speed * deltaSeconds, 0f);
            }
        }

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

package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.AreaLightHandle;
import com.viquelle.mikpik.light.LightHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.Collection;
import java.util.List;

public class CavesAmbientLightSource implements LightSource{
    private final AreaLightHandle lowerLight1 = new AreaLightHandle(
            (float) ( 1f * Math.PI), 80f, -4.5f, 0xFFFFFF, false, new Vector2f(128f,128f), false); // смотрит вверх
    private final AreaLightHandle lowerLight2 = new AreaLightHandle(
            (float) ( 1f * Math.PI), 160f, -4.5f, 0xFFFFFF, false, new Vector2f(128f,128f), false); // смотрит вниз
    private final AreaLightHandle upperLight1 = new AreaLightHandle(
            (float) ( 1f * Math.PI), 160f, -4.5f, 0xFFFFFF, false, new Vector2f(128f,128f), false); // смотрит вверх
    private final AreaLightHandle upperLight2 = new AreaLightHandle(
            (float) ( 1f * Math.PI), 80f, -4.5f, 0xFFFFFF, false, new Vector2f(128f,128f), false); // смотрит вниз
    private boolean registered = false;
    private final float LOWER_MAX_Y_HEIGHT = -16.0f;
    private final float LOWER_MIN_Y_HEIGHT = -512.0f;
    private final float UPPER_MAX_Y_HEIGHT = 32.0f;
    private final float UPPER_MIN_Y_HEIGHT = -512.0f;

    @Override
    public void tick(Level level, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null || !level.isClientSide) return;

        if (!registered) {
            lowerLight1.register();
            lowerLight2.register();
            upperLight1.register();
            upperLight2.register();
            registered = true;
        }

        tickLights(player, partialTick);
    }

    @Override
    public void destroy() {
        if (registered) {
            lowerLight1.unregister();
            lowerLight2.unregister();
            upperLight1.unregister();
            upperLight2.unregister();
            registered = false;
        }
    }

    @Override
    public Collection<? extends LightHandle> getLights() {
        return List.of(lowerLight1,lowerLight2, upperLight1,upperLight2);
    }

    private void tickLights(Player player, float partialTick) {
        Vec3 playerPos = player.getPosition(partialTick);
        float lowerY = (float) Math.clamp(playerPos.y - 48.0f, LOWER_MIN_Y_HEIGHT, LOWER_MAX_Y_HEIGHT);
        lowerLight1.setPosition((float) playerPos.x,lowerY, (float) playerPos.z);
        lowerLight2.setPosition((float) playerPos.x,lowerY, (float) playerPos.z);
        lowerLight1.setOrientation((float) Math.PI / 2, 0, 0);
        lowerLight2.setOrientation((float) -Math.PI / 2, 0, 0);

        float upperY = (float) Math.clamp(playerPos.y + 48.0f, UPPER_MIN_Y_HEIGHT, UPPER_MAX_Y_HEIGHT) - 0.001f; // от Z-Fighting
        upperLight1.setPosition((float) playerPos.x, upperY, (float) playerPos.z);
        upperLight2.setPosition((float) playerPos.x, upperY, (float) playerPos.z);
        upperLight1.setOrientation((float) Math.PI / 2, 0, 0);
        upperLight2.setOrientation((float) -Math.PI / 2, 0, 0);
        updateUpperLightBrightness(upperY);
    }

    private void updateUpperLightBrightness(float y) {
        float brightness = Math.clamp((32.0f - y) / 32.0f, 0.0f, 1.0f);
        brightness = Mth.lerp(brightness, 0.0f, -4.5f);
        upperLight1.setBrightness(brightness * 2);
        upperLight2.setBrightness(brightness);
    }
}

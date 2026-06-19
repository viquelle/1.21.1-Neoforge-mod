package com.viquelle.mikpik.client.coloredlights;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class ColorLightRenderer {
    public static final ColorLightRenderer INSTANCE = new ColorLightRenderer();

    private static final int MAX_LIGHTS = 256;
    private static final FloatBuffer posRadiusBuffer = BufferUtils.createFloatBuffer(MAX_LIGHTS * 4);
    private static final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(MAX_LIGHTS * 4);

    private static int currentLightCount = 0;

    private ColorLightRenderer() {}

    /**
     * Вызывается каждый кадр для загрузки видимых источников света в шейдер
     */
    public void tick(float gameTimeDeltaPartialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ColoredLightScanner.buildVisibleLightBuffer(mc.player);
        assert Minecraft.getInstance().cameraEntity != null;
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        updateLights(cameraPos);
    }

    public void updateLights(Vec3 cameraPos) {
        var activeLights = ColoredLightBuffer.get();
        ColoredLightBuffer.frame();
        currentLightCount = Math.min(activeLights.size(), MAX_LIGHTS);

        posRadiusBuffer.clear();
        colorBuffer.clear();

        for (int i = 0; i < currentLightCount; i++) {
            com.viquelle.mikpik.client.coloredlights.ActiveLight light = activeLights.get(i);

            // Позиция и радиус (xyz + w = radius)
            posRadiusBuffer.put((float) (light.x() - cameraPos.x));
            posRadiusBuffer.put((float) (light.y() - cameraPos.y));
            posRadiusBuffer.put((float) (light.z() - cameraPos.z));
            posRadiusBuffer.put(light.radius());

            // Цвет и интенсивность (rgb + a = intensity)
            colorBuffer.put(light.r());
            colorBuffer.put(light.g());
            colorBuffer.put(light.b());
            colorBuffer.put(light.intensity());
        }

        posRadiusBuffer.flip();
        colorBuffer.flip();
    }

    public static void uploadVanillaTerrainUniforms(int ID) {

        int u_light_count = GL20.glGetUniformLocation(ID, "u_light_count");
        int u_LightDataLocation = GL20.glGetUniformLocation(ID, "u_LightData");
        int u_LightColorLocation = GL20.glGetUniformLocation(ID, "u_LightColor");

        GL20.glUniform1i(u_light_count, currentLightCount);
        GL20.glUniform4fv(u_LightDataLocation, posRadiusBuffer);
        GL20.glUniform4fv(u_LightColorLocation, colorBuffer);

//        MikpikMod.LOGGER.info("<- {} {}", u_LightDataLocation, u_LightColorLocation);
    }
}
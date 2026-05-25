package com.viquelle.mikpik;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public class ColoredLightsShader {
    private int posBufferID = 0;
    private int colorBufferID = 1;

    public static void updateUniforms(int count, float[] posData, float[] colorData) {
        ShaderInstance shader = Minecraft.getInstance().gameRenderer.getShader("rendertype_solid");
        if (shader != null) {
            MikpikMod.LOGGER.info("shader not null");
            Uniform u_light_count = shader.getUniform("u_light_count");
            if (u_light_count != null) {
                MikpikMod.LOGGER.info("count not null");
                u_light_count.set(count);
                shader.apply();
            }
            Uniform u_lights_xyz_r = shader.getUniform("u_LightPosRadius");
            if (u_lights_xyz_r != null) {
                MikpikMod.LOGGER.info("posrad not null");
                u_lights_xyz_r.set(posData);
            }
            Uniform u_lights_rgb_i = shader.getUniform("u_LightColorIntensity");
            if (u_lights_rgb_i != null) {
                MikpikMod.LOGGER.info("colint not null");
                u_lights_rgb_i.set(colorData);
            }

        }
    }

}
package com.viquelle.mikpik;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class ColoredLightsShader {
    private static int lightsBufferID = -1;
    private static final int MAX_LIGHTS = 64;
    private static final int BYTES_PER_LIGHT = 2 * 16; // posRadius + colorIntensity

    public static void init() {
        if (lightsBufferID == -1) {
            lightsBufferID = GL43.glGenBuffers();

            // Создаём буфер максимального размера ОДИН РАЗ
            GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, lightsBufferID);
            GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER,
                    (long) MAX_LIGHTS * BYTES_PER_LIGHT,
                    GL43.GL_DYNAMIC_DRAW);

            // Привязываем к binding point 0 и не трогаем больше
            GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, lightsBufferID);

            MikpikMod.LOGGER.info("SSBO created and bound: id={}", lightsBufferID);
        }
    }

    public static void updateUniforms(int count, float[] posArray, float[] colArray) {
        if (lightsBufferID == -1) return;

        String[] renderTypes = {
                "rendertype_solid",
                "rendertype_cutout",
                "rendertype_cutout_mipped",
                "rendertype_translucent",
                "rendertype_translucent_moving_block",
                "rendertype_tripwire",
                "rendertype_end_portal",
                "rendertype_end_gateway"
        };

        for (String type : renderTypes) {
            ShaderInstance shader = Minecraft.getInstance().gameRenderer.getShader(type);
            if (shader == null) return;

            Uniform u_light_count = shader.getUniform("u_light_count");
            if (u_light_count != null) {
                u_light_count.set(count);
            }
        }

        updateBuffer(count,posArray,colArray);
    }

    public static void updateBuffer(int count, float[] posArray, float[] colArray) {
        if (count == 0) return;
        int totalFloats = count * 8;
        FloatBuffer combinedBuffer = MemoryUtil.memAllocFloat(totalFloats);

        try {
            for (int i = 0; i < count; i++) {
                combinedBuffer.put(posArray, i * 4, 4);
                combinedBuffer.put(colArray, i * 4, 4);
            }
            combinedBuffer.flip();

            // Обновляем данные в существующем буфере
            GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, lightsBufferID);
            GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, combinedBuffer);

        } finally {
            MemoryUtil.memFree(combinedBuffer);
        }
    }
    public static void cleanup() {
        if (lightsBufferID != -1) {
            GL43.glDeleteBuffers(lightsBufferID);
            lightsBufferID = -1;
        }
    }
}
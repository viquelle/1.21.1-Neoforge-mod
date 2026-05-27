package com.viquelle.mikpik.coloredlights;

import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.viquelle.mikpik.MikpikMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

public class ColoredLightsUploader {
    private static boolean shadersInitialized = false;
    private static int lightsBufferID = -1;
    private static final int MAX_LIGHTS = 64;
    private static final int BYTES_PER_LIGHT = 2 * 16; // 4 floats(16 bytes) per color/pos data
    private static final float[] POS_ARRAY = new float[MAX_LIGHTS * 4];
    private static final float[] COL_ARRAY = new float[MAX_LIGHTS * 4];
    private static final FloatBuffer UPLOAD_BUFFER = MemoryUtil.memAllocFloat(MAX_LIGHTS * 8);
    private static final String[] RENDER_TYPES = {
            "rendertype_solid",
            "rendertype_cutout",
            "rendertype_cutout_mipped",
            "rendertype_translucent",
            "rendertype_translucent_moving_block",
            "rendertype_tripwire"
    };
    private static final ShaderInstance[] SHADERS = new ShaderInstance[RENDER_TYPES.length];
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

    public static void initShaders() {
        Minecraft mc = Minecraft.getInstance();
        boolean success = true;
        if (!shadersInitialized) {
            for (int i = 0; i < RENDER_TYPES.length; i++) {
                if (SHADERS[i] != null) {
                    continue;
                }
                ShaderInstance shader = mc.gameRenderer.getShader(RENDER_TYPES[i]);
                if (shader == null) {
                    success = false;
                    continue;
                }
                SHADERS[i] = shader;
            }
        }
        shadersInitialized = success;
    }

    public static void updateUniforms(Vec3 cameraPos) {
        initShaders();
        if (lightsBufferID == -1) return;

        List<ActiveLight> list = ColoredLightBuffer.get();

        if (list.isEmpty()) {
            // Если нет источников, всё равно обновляем с 0
            updateBuffer(0, new float[0], new float[0]);
            for (ShaderInstance shader : SHADERS) {
                if (shader == null) {
                    continue;
                }

                Uniform u_light_count = shader.getUniform("u_light_count");
                if (u_light_count != null) {
                    u_light_count.set(0);
                }
            }
        } else {
            int maxLights = Math.min(list.size(), 64);
            float[] posArray = POS_ARRAY;
            float[] colArray = COL_ARRAY;

            for (int i = 0; i < maxLights; i++) {
                ActiveLight light = list.get(i);
                int offset = i * 4;
                posArray[offset]     = (float) (light.x() - cameraPos.x);
                posArray[offset + 1] = (float) (light.y() - cameraPos.y);
                posArray[offset + 2] = (float) (light.z() - cameraPos.z);
                posArray[offset + 3] = light.radius();

                colArray[offset]     = light.r();
                colArray[offset + 1] = light.g();
                colArray[offset + 2] = light.b();
                colArray[offset + 3] = light.intensity();
            }
            updateBuffer(maxLights, posArray, colArray);

            for (ShaderInstance shader : SHADERS) {
                if (shader == null) {
                    continue;
                }

                Uniform u_light_count = shader.getUniform("u_light_count");
                if (u_light_count != null) {
                    u_light_count.set(maxLights);
                }
            }
        }
    }

    public static void updateBuffer(int count, float[] posArray, float[] colArray) {
        if (count == 0) return;

        UPLOAD_BUFFER.clear();

        for (int i = 0; i < count; i++) {
            UPLOAD_BUFFER.put(posArray, i * 4, 4);
            UPLOAD_BUFFER.put(colArray, i * 4, 4);
        }
        UPLOAD_BUFFER.flip();

        // Обновляем данные в существующем буфере
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, lightsBufferID);

        GL43.glBufferData(
                GL43.GL_SHADER_STORAGE_BUFFER,
                (long) MAX_LIGHTS * BYTES_PER_LIGHT,
                GL43.GL_DYNAMIC_DRAW
        );
        GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, UPLOAD_BUFFER);
}
    public static void cleanup() {
        if (lightsBufferID != -1) {
            GL43.glDeleteBuffers(lightsBufferID);
            lightsBufferID = -1;
        }
    }
}

package com.viquelle.mikpik.coloredlights;

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
    private static final int BYTES_PER_LIGHT = 32; // 4 floats position + 4 floats color = 8 floats * 4 bytes
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
        if (lightsBufferID != -1) return;

        lightsBufferID = GL43.glGenBuffers();
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, lightsBufferID);
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, (long) MAX_LIGHTS * BYTES_PER_LIGHT, GL43.GL_DYNAMIC_DRAW);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, lightsBufferID);

        MikpikMod.LOGGER.info("ColoredLightsUploader initialized with buffer ID: {}", lightsBufferID);
    }

    public static void initShaders() {
        if (shadersInitialized) return;

        Minecraft mc = Minecraft.getInstance();
        boolean success = true;

        for (int i = 0; i < RENDER_TYPES.length; i++) {
            ShaderInstance shader = mc.gameRenderer.getShader(RENDER_TYPES[i]);
            if (shader == null) {
                success = false;
                continue;
            }
            SHADERS[i] = shader;
        }

        shadersInitialized = success;
        if (success) {
            MikpikMod.LOGGER.info("Shaders initialized for colored lights");
        } else {
            MikpikMod.LOGGER.warn("Failed to initialize some shaders for colored lights");
        }
    }

    public static void updateUniforms(Vec3 cameraPos) {
        initShaders();

        if (lightsBufferID == -1) {
            MikpikMod.LOGGER.warn("Buffer not initialized!");
            return;
        }

        List<ActiveLight> lights = ColoredLightBuffer.get();
        int count = Math.min(lights.size(), MAX_LIGHTS);

        if (count == 0) {
            setLightCount(0);
            return;
        }

        // Подготавливаем данные
        UPLOAD_BUFFER.clear();

        for (int i = 0; i < count; i++) {
            ActiveLight light = lights.get(i);

            // Позиция относительно камеры
            UPLOAD_BUFFER.put((float) (light.x() - cameraPos.x));
            UPLOAD_BUFFER.put((float) (light.y() - cameraPos.y));
            UPLOAD_BUFFER.put((float) (light.z() - cameraPos.z));
            UPLOAD_BUFFER.put(light.radius());

            // Цвет и интенсивность
            UPLOAD_BUFFER.put(light.r());
            UPLOAD_BUFFER.put(light.g());
            UPLOAD_BUFFER.put(light.b());
            UPLOAD_BUFFER.put(light.intensity());
        }

        UPLOAD_BUFFER.flip();

        // Загружаем в GPU
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, lightsBufferID);
        GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, UPLOAD_BUFFER);

        setLightCount(count);

//        if (count > 0) {
//            MikpikMod.LOGGER.debug("Uploaded {} lights to shader", count);
//        }
    }

    private static void setLightCount(int count) {
        for (ShaderInstance shader : SHADERS) {
            if (shader == null) continue;
            Uniform uniform = shader.getUniform("u_light_count");
            if (uniform != null) {
                uniform.set(count);
            }
        }
    }

    public static void cleanup() {
        if (lightsBufferID != -1) {
            GL43.glDeleteBuffers(lightsBufferID);
            lightsBufferID = -1;
        }
        MemoryUtil.memFree(UPLOAD_BUFFER);
    }
}
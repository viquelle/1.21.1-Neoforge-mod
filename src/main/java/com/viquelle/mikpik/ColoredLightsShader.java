package com.viquelle.mikpik;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

public class ColoredLightsShader {
    private static int posBufferID = -1;
    private static int colorBufferID = -1;
    private static boolean enabled = false;

    public static void init() {
        if (posBufferID == -1) {
            posBufferID = GL43.glGenBuffers();
            colorBufferID = GL43.glGenBuffers();
            MikpikMod.LOGGER.info("SSBO buffers created: pos={}, color={}", posBufferID, colorBufferID);
        }
    }

    public static void updateUniforms(int count, float[] posData, float[] colorData) {
        // Убеждаемся, что буферы созданы
        if (posBufferID == -1 || colorBufferID == -1) {
            MikpikMod.LOGGER.warn("SSBO not initialized, skipping update");
            return;
        }

        ShaderInstance shader = Minecraft.getInstance().gameRenderer.getShader("rendertype_solid");
        if (shader == null) {
            MikpikMod.LOGGER.warn("Shader is null");
            return;
        }

        Uniform u_light_count = shader.getUniform("u_light_count");
        if (u_light_count != null) {
            u_light_count.set(count);
            //MikpikMod.LOGGER.info("Set u_light_count to {}", count);
        } else {
            MikpikMod.LOGGER.warn("u_light_count uniform not found");
        }

        if (!enabled) {
            bindToShader(shader);
        }

        // Проверяем данные
        int expectedSize = count * 4;
        if (posData.length < expectedSize || colorData.length < expectedSize) {
            MikpikMod.LOGGER.error("Data size mismatch: expected {}, got pos={}, col={}",
                    expectedSize, posData.length, colorData.length);
            return;
        }

        // Обновляем буфер позиций
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, posBufferID);

        // Создаем буфер и заполняем
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(expectedSize);
        try {
            posBuffer.put(posData, 0, expectedSize).flip();
            // Выделяем память на GPU и копируем данные
            GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, (long) expectedSize * 4, GL43.GL_DYNAMIC_DRAW);
            GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, posBuffer);
        } catch (Exception e) {
            MikpikMod.LOGGER.error("Failed to upload pos buffer: {}", e.getMessage());
        } finally {
            MemoryUtil.memFree(posBuffer);
        }

        // Обновляем буфер цветов
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, colorBufferID);

        FloatBuffer colBuffer = MemoryUtil.memAllocFloat(expectedSize);
        try {
            colBuffer.put(colorData, 0, expectedSize).flip();
            GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, (long) expectedSize * 4, GL43.GL_DYNAMIC_DRAW);
            GL43.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, colBuffer);
        } catch (Exception e) {
            //MikpikMod.LOGGER.error("Failed to upload color buffer: {}", e.getMessage());
        } finally {
            MemoryUtil.memFree(colBuffer);
        }

        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);

        //MikpikMod.LOGGER.info("SSBO updated with {} lights", count);
    }

    public static void bindToShader(ShaderInstance shader) {
        if (posBufferID == -1 || colorBufferID == -1) {
            //MikpikMod.LOGGER.warn("Cannot bind: SSBO not initialized");
            return;
        }

        // Привязываем SSBO к binding points
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, posBufferID);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, colorBufferID);
        enabled = true;
        //MikpikMod.LOGGER.info("SSBO bound to shader (binding 0={}, binding 1={})", posBufferID, colorBufferID);
    }

    public static void cleanup() {
        if (posBufferID != -1) {
            GL43.glDeleteBuffers(posBufferID);
            posBufferID = -1;
        }
        if (colorBufferID != -1) {
            GL43.glDeleteBuffers(colorBufferID);
            colorBufferID = -1;
        }
        enabled = false;
        //MikpikMod.LOGGER.info("SSBO cleaned up");
    }
}
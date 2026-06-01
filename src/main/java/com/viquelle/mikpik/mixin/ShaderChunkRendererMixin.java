package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.coloredlights.ColorLightRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ShaderChunkRenderer.class}, remap = false)

public abstract class ShaderChunkRendererMixin {
    @Inject(
            method = "begin",
            at = @At("RETURN")
    )
    private void uploadColoredLights(TerrainRenderPass pass, CallbackInfo ci) {
        int programID = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        if (programID != 0) {
            ColorLightRenderer.uploadVanillaTerrainUniforms(programID);
        }
    }

}

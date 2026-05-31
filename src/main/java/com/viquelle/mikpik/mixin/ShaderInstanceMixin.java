package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.coloredlights.ColorLightRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ShaderInstance.class})
public abstract class ShaderInstanceMixin {

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract int getId();

    @Inject(
            method = {"apply"},
            at = {@At("TAIL")}
    )
    private void shine$uploadColoredLights(CallbackInfo ci) {
        if (isTerrainShader(this.getName())) {
            ColorLightRenderer.uploadVanillaTerrainUniforms(this.getId());
        }
    }

    private static boolean isTerrainShader(String name) {
        return "rendertype_solid".equals(name) || "rendertype_cutout".equals(name) || "rendertype_cutout_mipped".equals(name) || "rendertype_translucent".equals(name) || "rendertype_tripwire".equals(name);
    }
}

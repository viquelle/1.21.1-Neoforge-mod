package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ghost.GhostManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    private static final ResourceLocation GHOST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/ghost_player.png");

    @Inject(
            method = "getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getGhostRenderType(LivingEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (livingEntity instanceof Player && GhostManager.isGhost((Player) livingEntity)) {
            cir.setReturnValue(RenderType.entityTranslucentEmissive(GHOST_TEXTURE));
        }
    }

    @Inject(
            method = "getShadowRadius(Lnet/minecraft/world/entity/LivingEntity;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getShadowRadius(LivingEntity entity, CallbackInfoReturnable<Float> cir){
        if (entity instanceof Player player && GhostManager.isGhost(player)) {
            cir.setReturnValue(0.0f);
        }
    }
}
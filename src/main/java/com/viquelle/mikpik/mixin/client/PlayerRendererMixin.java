package com.viquelle.mikpik.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.client.ClientHeartManager;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.item.CustomArmPoseItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            method = "getArmPose",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void MIKPIK$onGetArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof CustomArmPoseItem armPoseItem) {
            HumanoidModel.ArmPose pose = armPoseItem.getArmPose(stack, player, hand);
            if (pose != null) {
                cir.setReturnValue(pose);
            }
        }
    }

    private static final ResourceLocation GHOST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/ghost_player.png");

    @Inject(
            method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ghostRenderType(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (GhostManager.isGhost(entity)) {
            cir.setReturnValue(GHOST_TEXTURE);
        }
    }

    @Redirect(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/PlayerSkin;texture()Lnet/minecraft/resources/ResourceLocation;")
    )
    private ResourceLocation ghostHandTexture(PlayerSkin instance) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && GhostManager.isGhost(player)) {
            return GHOST_TEXTURE;
        }
        return instance.texture();
    }

    @Redirect(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer ghostHandBuffer(MultiBufferSource instance, RenderType renderType) {
        Player player = Minecraft.getInstance().player;

        if (player != null && GhostManager.isGhost(player)) {
            return instance.getBuffer(
                    RenderType.entityTranslucentEmissive(GHOST_TEXTURE)
            );
        }
        return instance.getBuffer(renderType);
    }

//    @Inject(
//            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void cancelGhostRenderer(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci){
//        if (GhostManager.isGhost(entity)) {
//            ci.cancel();
//        }
//    }

    @Inject(method = "render", at = @At("HEAD"))
    private void mikpik$startRender(
        AbstractClientPlayer player,
        float entityYaw,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        ClientHeartManager.renderingPlayer = player;
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void mikpik$endRender(
        AbstractClientPlayer player,
        float entityYaw,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        ClientHeartManager.renderingPlayer = null;
    }
}

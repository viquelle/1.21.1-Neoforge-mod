package com.viquelle.mikpik.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.viquelle.mikpik.client.ClientHeartManager;
import com.viquelle.mikpik.item.items.HeartItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class HeartItemEntityRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void mikpik$scaleHeart(ItemEntity entity, float entityYaw, float partialTick,
                                   PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        ItemStack stack = entity.getItem();
        if (!(stack.getItem() instanceof HeartItem)) return;
        if (!HeartItem.isCharged(stack)) return;

        float scale = ClientHeartManager.getWorldScale(entity, partialTick);

        poseStack.translate(0.0f, -entity.getBbHeight()/2, 0.0f);
        poseStack.scale(scale, scale - (scale - 1.0f)/2, scale);
        poseStack.translate(0.0f, +entity.getBbHeight()/2, 0.0f);
    }


}

package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.client.ClientHeartManager;
import com.viquelle.mikpik.item.items.HeartItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class HeartItemRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void mikpik$scaleHeart(
            ItemStack stack,
            ItemDisplayContext displayContext,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int combinedLight,
            int combinedOverlay,
            BakedModel model,
            CallbackInfo ci
    ) {
        if (!(stack.getItem() instanceof HeartItem)) return;
        if (!HeartItem.isCharged(stack)) return;

        float scale = ClientHeartManager.getInventoryScale(stack, displayContext);
        poseStack.scale(scale, scale, scale);
    }
}
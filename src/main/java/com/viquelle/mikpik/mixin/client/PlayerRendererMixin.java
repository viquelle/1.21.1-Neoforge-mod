package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.item.CustomArmPoseItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            method = "getArmPose",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void MIKPIK$onGetArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir){
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof CustomArmPoseItem armPoseItem) {
            HumanoidModel.ArmPose pose = armPoseItem.getArmPose(stack, player, hand);
            if (pose != null) {
                cir.setReturnValue(pose);
            }
        }
    }
}

package com.viquelle.mikpik.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface CustomArmPoseItem {
    HumanoidModel.ArmPose getArmPose(ItemStack stack, Player player, InteractionHand hand);
}

package com.viquelle.mikpik.sanity.factor;

import com.viquelle.mikpik.item.FlowerCrownItem;
import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FlowerCrownFactor implements SanityFactor{

    @Override
    public float getDelta(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);

        if (!(head.getItem() instanceof FlowerCrownItem)) {
            return 0.0f;
        }

        head.hurtAndBreak(1, player, EquipmentSlot.HEAD);

        return SanityConstants.FLOWER_CROWN_REGEN_PER_TICK;
    }
}

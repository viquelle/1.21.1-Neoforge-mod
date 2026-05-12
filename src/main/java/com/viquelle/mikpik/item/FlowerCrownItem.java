package com.viquelle.mikpik.item;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FlowerCrownItem extends ArmorItem {
    private static final int REGEN_INTERVAL_TICKS = 20;
    private static final float SANITY_REGEN = 0.5f;

    public FlowerCrownItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

}

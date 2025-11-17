package com.viquelle.examplemod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class FlashlightItem extends Item {
    private boolean isOn = false;

    public FlashlightItem(Item.Properties props){
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        var tag = data.copyTag();

        boolean enabled = tag.getBoolean("enabled");

        enabled = !enabled;
        tag.putBoolean("enabled", enabled);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (!level.isClientSide) {
            isOn = !isOn;
            String message = isOn ? "ВКЛ" : "ВЫКЛ";
            player.displayClientMessage(Component.literal(enabled ? "вкл" : "выкл"), true);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}

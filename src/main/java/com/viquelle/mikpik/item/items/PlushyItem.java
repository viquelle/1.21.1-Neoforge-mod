package com.viquelle.mikpik.item.items;

import com.viquelle.mikpik.registry.ModDataComponents;
import com.viquelle.mikpik.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PlushyItem extends Item {
    public static final float MAX_STORED_SANITY = 100.0f;


    public PlushyItem(Properties properties) {
        super(properties
                .stacksTo(1)
                .component(ModDataComponents.PLUSHY_STORED_SANITY.get(), 0.0f)
                .component(ModDataComponents.PLUSHY_ID.get(), ""));
    }

    public static float getStored(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.PLUSHY_STORED_SANITY.get(), 0.0f);
    }

    public static void setStored(ItemStack stack, float value) {
        stack.set(ModDataComponents.PLUSHY_STORED_SANITY.get(), clamp(value));
    }

    public static void addStored(ItemStack stack, float value) {
        setStored(stack, getStored(stack) + value);
    }

    private static String genAndSetID(ItemStack stack) {
        String uuid = java.util.UUID.randomUUID().toString();
        stack.set(ModDataComponents.PLUSHY_ID.get(), uuid);
        return uuid;
    }

    public static String getID(ItemStack stack) {
        String id = stack.getOrDefault(ModDataComponents.PLUSHY_ID.get(), "");

        if (id.isEmpty()) {
            id = genAndSetID(stack);
        }

        return id;
    }

    private static float clamp(float v) {
        return Math.max(0.0f, Math.min(MAX_STORED_SANITY, v));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        String activeID = player.getData(ModAttachments.ACTIVE_PLUSHY_ID);
        String selfID = getID(stack);


        if (activeID.isEmpty()) {
            player.setData(ModAttachments.ACTIVE_PLUSHY_ID, selfID);
            return;
        }

        if (!activeID.equals(selfID)) {
            ItemStack copy = stack.copy();
            stack.setCount(0);
            player.drop(copy, true, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("ID: " + getID(stack)));
        tooltipComponents.add(Component.literal("Stored: " + getStored(stack)));
    }
}

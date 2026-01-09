package com.viquelle.examplemod.item;

import com.viquelle.examplemod.ExampleMod;
import com.viquelle.examplemod.client.ClientLightManager;
import com.viquelle.examplemod.client.light.AbstractLight;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public abstract class AbstractLightItem extends Item {
    private static final String TAG_ENABLED = "enabled";
    private static final String TAG_FUEL = "fuel";
    private static final String TAG_COOLDOWN = "light_cooldown";

    public record LightSettings(
            AbstractLightItem.LightType type,
            int color,
            float brightness,
            float radius,
            float angle,
            float distance
    ) {}

    public abstract LightSettings getSettings(ItemStack stack);
    public AbstractLightItem(Properties properties) {
        super(properties);
    }

    public abstract String getKey(Player player);

    public enum LightType {
        POINT,
        AREA
    }
    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TAG_ENABLED);
    }

    public static void toggleTo(ItemStack stack, boolean value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putBoolean(TAG_ENABLED,value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void toggle(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putBoolean(TAG_ENABLED, !tag.getBoolean(TAG_ENABLED));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setFuel(ItemStack stack, int value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putInt(TAG_FUEL, value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getFuel(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(TAG_FUEL);
    }

    public static void consumeFuel(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putInt(TAG_FUEL, Math.max(0,getFuel(stack) - 1));
    }

    public static int getCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt(TAG_COOLDOWN);
    }

    public static void setCooldown(ItemStack stack, int ticks) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();
        data.putInt(TAG_COOLDOWN, ticks);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) {
            int cd = getCooldown(stack);
            if (cd > 0) {
                setCooldown(stack, cd - 1);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (getCooldown(stack) > 0) return InteractionResultHolder.fail(stack);

        toggle(stack);
        setCooldown(stack,20);
        ExampleMod.LOGGER.info("{}",isEnabled(stack));

        if (level.isClientSide) {
            boolean enabled = isEnabled(stack);
            player.displayClientMessage(Component.literal(enabled ? "ON" : "OFF"), true);
        }

        level.playSound(player, player.getX(),player.getY(),player.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

}

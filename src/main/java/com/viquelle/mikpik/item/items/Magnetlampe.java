package com.viquelle.mikpik.item.items;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.registry.ModDataComponents;
import com.viquelle.mikpik.item.CustomArmPoseItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class Magnetlampe extends Item implements CustomArmPoseItem {
    public static final int MAX_CHARGE = 1*60*20;
    private static final int PERFECT_TIME = 16;
    private static final int WINDOW = 10;
    public Magnetlampe(Properties properties) {
        super(properties
                .stacksTo(1)
                .component(ModDataComponents.CHARGE_TICKS.get(), 0)
                .component(ModDataComponents.LAST_UPDATE.get(), 0L)
                .component(ModDataComponents.CHARGING_START.get(), 0L));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            stack.set(ModDataComponents.CHARGING_START.get(), level.getGameTime());
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (level.isClientSide) return;
        Player player = (Player) entity;
        long startTime = stack.getOrDefault(ModDataComponents.CHARGING_START.get(),0L);

        if (startTime == -1L) {
            stack.set(ModDataComponents.CHARGING_START.get(), 0L);
            return;
        }

        long heldTime = level.getGameTime() - startTime;

        float distance = Math.abs(heldTime - PERFECT_TIME);
        float efficiency = 0.0f;

        if (distance <= WINDOW) {
            efficiency = 1.0f - (distance / WINDOW) * (distance / WINDOW);
        }

        int chargeToAdd = (int) (MAX_CHARGE * 0.4f * efficiency);
        addCharge(stack, level, chargeToAdd);

        stack.set(ModDataComponents.CHARGING_START.get(), 0L);

        if (efficiency > 0) {
            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    1f,
                    0.8f + efficiency * 0.4f
            );
        } else {
            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.FLINTANDSTEEL_USE,
                    SoundSource.PLAYERS,
                    1f,
                    0.9f
            );
        }

        if (efficiency >= 0.95f) {
            player.displayClientMessage(Component.translatable("message." + MikpikMod.MODID + ".wind_perfect").withColor(0x00FF00), true);
        } else if (efficiency >= 0.8f) {
            player.displayClientMessage(Component.translatable("message." + MikpikMod.MODID + ".wind_good").withColor(0xFFFF00), true);
        } else if (efficiency > 0) {
            player.displayClientMessage(Component.translatable("message." + MikpikMod.MODID + ".wind_weak").withColor(0xFFA600), true);
        } else {
            player.displayClientMessage(Component.translatable("message." + MikpikMod.MODID + ".wind_miss").withColor(0xBDBDBD), true);
        }

    }

    @Override
    public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remainingTicks) {
        if (level.isClientSide) return;

        long startTime = stack.getOrDefault(ModDataComponents.CHARGING_START.get(),0L);
        long heldTime = level.getGameTime() - startTime;

        if (heldTime > PERFECT_TIME + WINDOW) {
            stack.set(ModDataComponents.CHARGING_START.get(),-1L);
            level.playSound(
                    null,
                    entity.blockPosition(),
                    SoundEvents.STONE_BUTTON_CLICK_OFF,
                    SoundSource.PLAYERS,
                    0.5f,
                    0.8f
            );

            if (entity instanceof Player player) {
                player.sendSystemMessage(Component.literal("✗ Передержал!").withStyle(ChatFormatting.RED));
                player.stopUsingItem();
            }
        }

    }


    public static int getCharge(ItemStack stack, Level level) {
        int charge = stack.getOrDefault(ModDataComponents.CHARGE_TICKS.get(), 0);
        long last_update = stack.getOrDefault(ModDataComponents.LAST_UPDATE.get(), 0L);

        long elapsed = level.getGameTime() - last_update;
        return Math.max(charge - (int)elapsed, 0);
    }

    public static void setCharge(ItemStack stack, Level level, int charge) {
        int current = getCharge(stack, level);

        stack.set(ModDataComponents.CHARGE_TICKS, charge);
        stack.set(ModDataComponents.LAST_UPDATE, level.getGameTime());
    }

    public static void addCharge(ItemStack stack, Level level, int amount) {
        setCharge(stack, level, Math.min(getCharge(stack,level) + amount, MAX_CHARGE));
    }

    public static float getPercent(ItemStack stack, Level level) {
        return getCharge(stack, level) / (float) MAX_CHARGE;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(
            ItemStack stack,
            Player player,
            InteractionHand hand) {

        if (player.isUsingItem() &&
                player.getUseItem() == stack) {

            int useTime =
                    player.getTicksUsingItem();

            if (useTime > 10) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
        }

        if (getCharge(stack, player.level()) > 0) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }

        return null;
    }
}

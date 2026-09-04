package com.viquelle.mikpik.item.items;

import com.viquelle.mikpik.ghost.HealthPenailtyUtil;
import net.minecraft.server.level.ServerPlayer;
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

public class LifeInjectorItem extends Item {
    public LifeInjectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);

        if (!level.isClientSide) {
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.WOODEN_BUTTON_CLICK_OFF,
                    SoundSource.PLAYERS,
                    0.35F,
                    0.75F
            );
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            HealthPenailtyUtil.reducePenalty(player, 0.2);
            stack.shrink(1);
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.BOTTLE_EMPTY,
                    SoundSource.PLAYERS,
                    0.3F,
                    0.75F
            );
        }

        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (player.isUsingItem() && player.getUseItem() == stack) {
            int remaining = player.getUseItemRemainingTicks();

            // 40 -> 0, поэтому 20 = середина анимации
            if (remaining == 20) {
                player.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.ARMOR_EQUIP_LEATHER.value(),
                        SoundSource.PLAYERS,
                        0.3F,
                        0.8F
                );
            }
        }
    }
}

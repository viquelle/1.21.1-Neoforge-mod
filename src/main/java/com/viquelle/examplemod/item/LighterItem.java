package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.ClientLightManager;
import com.viquelle.examplemod.client.light.AbstractLight;
import com.viquelle.examplemod.client.light.PointLight;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LighterItem extends AbstractLightItem {

    public LighterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            if (getCooldown(stack) > 0) {
                return InteractionResultHolder.pass(stack);
            }

            boolean enabled = !isEnabled(stack);
            toggle(stack);
            setCooldown(stack, 10);

            String key = "lighter_" + player.getUUID();
            AbstractLight<?> existing = ClientLightManager.getLight(key);

            if (enabled) {
                if (existing == null) {
                    PointLight light = new PointLight.Builder()
                            .setPlayer(player)
                            .setBrightness(1.0f)
                            .setColor(0xFFFFFF)
                            .build();
                    ClientLightManager.add(key, light);
                } else {
                    existing.brightness = 1.0f;
                }
            } else {
                if (existing != null) {
                    ClientLightManager.remove(key);
                }
            }

            player.displayClientMessage(Component.literal(enabled ? "ON" : "OFF"), true);
        }

        return InteractionResultHolder.pass(stack);
    }
}
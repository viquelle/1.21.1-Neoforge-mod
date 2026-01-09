package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.ClientLightManager;
import com.viquelle.examplemod.client.light.AreaLight;
import foundry.veil.api.client.registry.LightTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FlashlightItem extends AbstractLightItem {
    public static final String ITEM_NAME = "flashlight";

    @Override
    public LightSettings getSettings(ItemStack stack) {
        return new LightSettings(LightType.AREA, 0xB4B4FF, 1f, 0f, 0.6f, 32f);
    }

    public FlashlightItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public String getKey(Player player) {
        return ITEM_NAME + "_" + player.getUUID();
    }


}
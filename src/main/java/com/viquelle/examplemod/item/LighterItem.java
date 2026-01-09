package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.ClientLightManager;
import com.viquelle.examplemod.client.light.AbstractLight;
import com.viquelle.examplemod.client.light.PointLight;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LighterItem extends AbstractLightItem {
    public static final String ITEM_NAME = "lighter";

    @Override
    public LightSettings getSettings(ItemStack stack) {
        return new LightSettings(LightType.POINT, 0xFFAA33, 1f, 6f, 0f, 0f);
    }

    public LighterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public String getKey(Player player) {
        return ITEM_NAME + "_" + player.getUUID();
    }


}
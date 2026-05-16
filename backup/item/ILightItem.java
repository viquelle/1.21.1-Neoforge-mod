package com.viquelle.mikpik.item;

import com.viquelle.mikpik.light.AbstractLight;
import net.minecraft.world.entity.player.Player;

public interface ILightItem {
    AbstractLight<?> createLight(Player player);
}

package com.viquelle.examplemod.client.light;

import net.minecraft.client.player.LocalPlayer;

public interface IPlayerLight {
    IPlayerLight create();
    void destroy();
    void tick(LocalPlayer player, float partialTick);
    boolean isActive();
    void setColor(int color);
}

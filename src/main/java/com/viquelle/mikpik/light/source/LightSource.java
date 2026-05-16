package com.viquelle.mikpik.light.source;

import net.minecraft.world.level.Level;

public interface LightSource {
    void tick(Level level, float partialTick);
    void destroy();
}
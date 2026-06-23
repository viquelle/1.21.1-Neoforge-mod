package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.LightHandle;
import net.minecraft.world.level.Level;

import java.util.Collection;

public interface LightSource {
    void tick(Level level, float partialTick);
    void destroy();
    Collection<? extends LightHandle> getLights();
}
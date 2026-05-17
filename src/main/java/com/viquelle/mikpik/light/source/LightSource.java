package com.viquelle.mikpik.light.source;

import com.viquelle.mikpik.light.LightHandle;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public interface LightSource {
    void tick(Level level, float partialTick);
    void destroy();
    Collection<? extends LightHandle> getLights();
}
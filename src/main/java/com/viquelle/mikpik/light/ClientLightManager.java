package com.viquelle.mikpik.light;

import com.viquelle.mikpik.light.source.LightSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

public class ClientLightManager {
    private static final List<LightSource> SOURCES = new ArrayList<>();

    public static void register(LightSource source) {
        SOURCES.add(source);
    }

    public static void tick(Level level, Player player, float partialTick) {
        for (LightSource source : SOURCES) {
            source.tick(level, partialTick);
        }
    }

    public static void clear() {
        SOURCES.forEach(LightSource::destroy);
        SOURCES.clear();
    }
}
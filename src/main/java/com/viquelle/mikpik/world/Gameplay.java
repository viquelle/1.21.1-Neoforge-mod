package com.viquelle.mikpik.world;

import com.viquelle.mikpik.network.payload.GameplayModePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Gameplay {

    private Gameplay() {}

    private static GameplayMode clientMode = GameplayMode.DST;

    public static GameplayMode get(Level level) {
        if (level instanceof ServerLevel server) {
            return GameplaySettings.get(server).getMode();
        }

        return clientMode;
    }

    public static boolean isDst(Level level) {
        return get(level) == GameplayMode.DST;
    }

    public static void set(ServerLevel level, GameplayMode mode) {
        GameplaySettings.get(level).setMode(mode);

        GameplayModePayload payload = new GameplayModePayload(mode);
        PacketDistributor.sendToAllPlayers(payload);
    }

    // Только пакет вызывает этот метод
    public static void setClientMode(GameplayMode mode) {
        clientMode = mode;
    }
}
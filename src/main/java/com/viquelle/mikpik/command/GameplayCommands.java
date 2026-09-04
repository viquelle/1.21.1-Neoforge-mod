package com.viquelle.mikpik.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.viquelle.mikpik.world.Gameplay;
import com.viquelle.mikpik.world.GameplayMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class GameplayCommands {

    private GameplayCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mode")

                .executes(context -> getMode(context.getSource()))

                .then(
                        Commands.literal("dst")
                                .executes(ctx ->
                                        setMode(ctx.getSource(), GameplayMode.DST)
                                )
                )

                .then(
                        Commands.literal("vanilla")
                                .executes(ctx ->
                                        setMode(ctx.getSource(), GameplayMode.VANILLA)
                                )
                );
    }

    private static int getMode(CommandSourceStack source) {
        GameplayMode mode = Gameplay.get(source.getLevel());

        source.sendSuccess(
                () -> Component.literal("Current gameplay mode: " + mode),
                false
        );

        return 1;
    }

    private static int setMode(CommandSourceStack source, GameplayMode mode) {
        ServerLevel level = source.getLevel();

        Gameplay.set(level, mode);

        source.sendSuccess(
                () -> Component.literal("Gameplay mode changed to " + mode),
                true
        );

        return 1;
    }
}
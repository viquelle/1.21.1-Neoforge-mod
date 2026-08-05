package com.viquelle.mikpik;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.viquelle.mikpik.world.Gameplay;
import com.viquelle.mikpik.world.GameplayMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mikpik")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("mode")
                                        .executes(ModCommands::getMode)
                                        .then(
                                                Commands.literal("dst")
                                                        .executes(ctx -> setMode(ctx, GameplayMode.DST))
                                        )
                                        .then(
                                                Commands.literal("vanilla")
                                                        .executes(ctx -> setMode(ctx, GameplayMode.VANILLA))
                                        )
                        )
        );
    }

    private static int getMode(CommandContext<CommandSourceStack> context) {
        GameplayMode mode = Gameplay.get(context.getSource().getLevel());

        context.getSource().sendSuccess(
                () -> Component.literal("Current gameplay mode: " + mode),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int setMode(CommandContext<CommandSourceStack> context, GameplayMode mode) {
        ServerLevel level = context.getSource().getLevel();

        Gameplay.set(level, mode);

        context.getSource().sendSuccess(
                () -> Component.literal("Gameplay mode changed to " + mode),
                true
        );

        return Command.SINGLE_SUCCESS;
    }
}
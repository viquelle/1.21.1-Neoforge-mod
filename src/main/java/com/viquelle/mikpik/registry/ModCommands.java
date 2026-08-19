package com.viquelle.mikpik.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.command.GameplayCommands;
import com.viquelle.mikpik.command.GhostCommands;
import com.viquelle.mikpik.command.SanityCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class ModCommands {
    private ModCommands() {

    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(MikpikMod.MODID)
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                        .then(SanityCommands.build())
                        .then(GameplayCommands.build())
                        .then(GhostCommands.build())
        );
    }
}
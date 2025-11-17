package com.viquelle.examplemod.sanity;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class SanityCommands {
    public static void logMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
                Commands.literal("sanity")
                        .requires(src -> src.hasPermission(0))
                        .then(Commands.literal("get")
                                .executes(SanityCommands::getSanity)
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(SanityCommands::setSanity)
                                )
                        )
        );
    }

    private static int getSanity(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            logMessage(player, "Sanity: " + SanityUtil.getSanity(player));
        }
        return 1;
    }

    private static int setSanity(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "value");
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            SanityUtil.set(player,value);
            logMessage(player,"Установлено "+SanityUtil.getSanity(player)+" рассудка ");
        }
        return 1;
    }
}

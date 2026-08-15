package com.viquelle.mikpik.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.viquelle.mikpik.sanity.SanitySystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;

public final class SanityCommands {

    private SanityCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("sanity")

                .then(
                        Commands.literal("get")
                                .executes(ctx ->
                                        getSanity(
                                                ctx.getSource(),
                                                ctx.getSource().getPlayerOrException()
                                        )
                                )
                                .then(
                                        Commands.argument("target", EntityArgument.player())
                                                .executes(ctx ->
                                                        getSanity(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "target")
                                                        )
                                                )
                                )
                )

                .then(
                        Commands.literal("set")
                                .then(
                                        Commands.argument("value", floatArg())
                                                .executes(ctx ->
                                                        setSanity(
                                                                ctx.getSource(),
                                                                ctx.getSource().getPlayerOrException(),
                                                                getFloat(ctx, "value")
                                                        )
                                                )
                                                .then(
                                                        Commands.argument("target", EntityArgument.player())
                                                                .executes(ctx ->
                                                                        setSanity(
                                                                                ctx.getSource(),
                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                getFloat(ctx, "value")
                                                                        )
                                                                )
                                                )
                                )
                )

                .then(
                        Commands.literal("add")
                                .then(
                                        Commands.argument("value", floatArg())
                                                .executes(ctx ->
                                                        addSanity(
                                                                ctx.getSource(),
                                                                ctx.getSource().getPlayerOrException(),
                                                                getFloat(ctx, "value")
                                                        )
                                                )
                                                .then(
                                                        Commands.argument("target", EntityArgument.player())
                                                                .executes(ctx ->
                                                                        addSanity(
                                                                                ctx.getSource(),
                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                getFloat(ctx, "value")
                                                                        )
                                                                )
                                                )
                                )
                )

                .then(
                        Commands.literal("remove")
                                .then(
                                        Commands.argument("value", floatArg())
                                                .executes(ctx ->
                                                        removeSanity(
                                                                ctx.getSource(),
                                                                ctx.getSource().getPlayerOrException(),
                                                                getFloat(ctx, "value")
                                                        )
                                                )
                                                .then(
                                                        Commands.argument("target", EntityArgument.player())
                                                                .executes(ctx ->
                                                                        removeSanity(
                                                                                ctx.getSource(),
                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                getFloat(ctx, "value")
                                                                        )
                                                                )
                                                )
                                )
                );
    }

    private static int getSanity(CommandSourceStack source, Player player) {
        float current = SanitySystem.get(player);

        source.sendSuccess(
                () -> Component.literal(
                        "Рассудок " +
                                player.getName().getString() +
                                ": " +
                                String.format("%.1f", current)
                ),
                false
        );

        return 1;
    }

    private static int setSanity(CommandSourceStack source, Player player, float value) {
        SanitySystem.set(player, value);

        source.sendSuccess(
                () -> Component.literal(
                        "Установлен рассудок " +
                                player.getName().getString() +
                                ": " +
                                String.format("%.1f", SanitySystem.get(player))
                ),
                false
        );

        return 1;
    }

    private static int addSanity(CommandSourceStack source, Player player, float value) {
        SanitySystem.add(player, value);

        source.sendSuccess(
                () -> Component.literal(
                        "Добавлено " +
                                value +
                                " к рассудку " +
                                player.getName().getString() +
                                ". Текущий: " +
                                String.format("%.1f", SanitySystem.get(player))
                ),
                false
        );

        return 1;
    }

    private static int removeSanity(CommandSourceStack source, Player player, float value) {
        SanitySystem.drain(player, value);

        source.sendSuccess(
                () -> Component.literal(
                        "Отнято " +
                                value +
                                " от рассудка " +
                                player.getName().getString() +
                                ". Текущий: " +
                                String.format("%.1f", SanitySystem.get(player))
                ),
                false
        );

        return 1;
    }
}
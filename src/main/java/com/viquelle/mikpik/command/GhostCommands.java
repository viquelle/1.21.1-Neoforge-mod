package com.viquelle.mikpik.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.ghost.HealthPenailtyUtil;
import com.viquelle.mikpik.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;

public final class GhostCommands {

    private GhostCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("ghost")
                // /mikpik ghost get [player]
                .then(
                        Commands.literal("get")
                                .executes(ctx ->
                                        getGhost(
                                                ctx.getSource(),
                                                ctx.getSource().getPlayerOrException()
                                        )
                                )
                                .then(
                                        Commands.argument("target", EntityArgument.player())
                                                .executes(ctx ->
                                                        getGhost(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "target")
                                                        )
                                                )
                                )
                )

                // /mikpik ghost set <player> <true|false>
                .then(
                        Commands.literal("set")
                                .then(
                                        Commands.argument("target", EntityArgument.player())
                                                .then(
                                                        Commands.argument("ghost", bool())
                                                                .executes(ctx ->
                                                                        setGhost(
                                                                                ctx.getSource(),
                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                getBool(ctx, "ghost")
                                                                        )
                                                                )
                                                )
                                )
                )

                // /mikpik ghost revive <player>
                .then(
                        Commands.literal("revive")
                                .then(
                                        Commands.argument("target", EntityArgument.player())
                                                .executes(ctx ->
                                                        revive(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "target")
                                                        )
                                                )
                                )
                )

                // /mikpik ghost penalty ...
                .then(
                        Commands.literal("penalty")

                                // /mikpik ghost penalty get [player]
                                .then(
                                        Commands.literal("get")
                                                .executes(ctx ->
                                                        getPenalty(
                                                                ctx.getSource(),
                                                                ctx.getSource().getPlayerOrException()
                                                        )
                                                )
                                                .then(
                                                        Commands.argument("target", EntityArgument.player())
                                                                .executes(ctx ->
                                                                        getPenalty(
                                                                                ctx.getSource(),
                                                                                EntityArgument.getPlayer(ctx, "target")
                                                                        )
                                                                )
                                                )
                                )

                                // /mikpik ghost penalty add <player> <amount>
                                .then(
                                        Commands.literal("add")
                                                .then(
                                                        Commands.argument("target", EntityArgument.player())
                                                                .then(
                                                                        Commands.argument("amount", doubleArg(0.0, 1.0))
                                                                                .executes(ctx ->
                                                                                        addPenalty(
                                                                                                ctx.getSource(),
                                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                                getDouble(ctx, "amount")
                                                                                        )
                                                                                )
                                                                )
                                                )
                                )

                                // /mikpik ghost penalty add-soft <player> <amount>
                                .then(
                                        Commands.literal("add-soft")
                                                .then(
                                                        Commands.argument("target", EntityArgument.player())
                                                                .then(
                                                                        Commands.argument("amount", doubleArg(0.0, 1.0))
                                                                                .executes(ctx ->
                                                                                        addPenaltySoft(
                                                                                                ctx.getSource(),
                                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                                getDouble(ctx, "amount")
                                                                                        )
                                                                                )
                                                                )
                                                )
                                )

                                // /mikpik ghost penalty remove <player> <amount>
                                .then(
                                        Commands.literal("remove")
                                                .then(
                                                        Commands.argument("target", EntityArgument.player())
                                                                .then(
                                                                        Commands.argument("amount", doubleArg(0.0, 1.0))
                                                                                .executes(ctx ->
                                                                                        removePenalty(
                                                                                                ctx.getSource(),
                                                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                                                getDouble(ctx, "amount")
                                                                                        )
                                                                                )
                                                                )
                                                )
                                )
                );
    }

    private static int getGhost(CommandSourceStack source, Player player) {
        boolean ghost = GhostManager.isGhost(player);

        source.sendSuccess(
                () -> Component.literal(
                        player.getName().getString()
                                + " ghost: "
                                + ghost
                ),
                false
        );

        return 1;
    }

    private static int setGhost(
            CommandSourceStack source,
            Player player,
            boolean ghost
    ) {
        if (ghost) {
            GhostManager.becomeGhost(player);

            source.sendSuccess(
                    () -> Component.literal(
                            player.getName().getString()
                                    + " стал призраком."
                    ),
                    true
            );
        } else {
            GhostManager.revive(player);

            source.sendSuccess(
                    () -> Component.literal(
                            player.getName().getString()
                                    + " был возрожден."
                    ),
                    true
            );
        }

        return 1;
    }

    private static int revive(
            CommandSourceStack source,
            Player player
    ) {
        if (!GhostManager.isGhost(player)) {
            source.sendFailure(
                    Component.literal(
                            player.getName().getString()
                                    + " не является призраком."
                    )
            );

            return 0;
        }

        GhostManager.revive(player);

        source.sendSuccess(
                () -> Component.literal(
                        player.getName().getString()
                                + " был возрожден."
                ),
                true
        );

        return 1;
    }

    private static int getPenalty(
            CommandSourceStack source,
            Player player
    ) {
        double penalty = player.getData(ModAttachments.PENALTY);

        source.sendSuccess(
                () -> Component.literal(
                        "Penalty "
                                + player.getName().getString()
                                + ": "
                                + String.format("%.2f", penalty)
                                + " ("
                                + String.format("%.0f", penalty * 100)
                                + "%)"
                ),
                false
        );

        return 1;
    }

    private static int addPenalty(
            CommandSourceStack source,
            Player player,
            double amount
    ) {
        HealthPenailtyUtil.addPenaltyHard(player, amount);

        return sendPenaltyResult(
                source,
                player,
                "Добавлен penalty: " + formatPercent(amount)
        );
    }

    private static int addPenaltySoft(
            CommandSourceStack source,
            Player player,
            double amount
    ) {
        HealthPenailtyUtil.addPenaltySoft(player, amount);

        return sendPenaltyResult(
                source,
                player,
                "Добавлен soft penalty: " + formatPercent(amount)
        );
    }

    private static int removePenalty(
            CommandSourceStack source,
            Player player,
            double amount
    ) {
        HealthPenailtyUtil.reducePenalty(player, amount);

        return sendPenaltyResult(
                source,
                player,
                "Убран penalty: " + formatPercent(amount)
        );
    }

    private static int sendPenaltyResult(
            CommandSourceStack source,
            Player player,
            String action
    ) {
        double current = player.getData(ModAttachments.PENALTY);

        source.sendSuccess(
                () -> Component.literal(
                        action
                                + ". "
                                + player.getName().getString()
                                + ": "
                                + String.format("%.2f", current)
                                + " ("
                                + String.format("%.0f", current * 100)
                                + "%)"
                ),
                true
        );

        return 1;
    }

    private static String formatPercent(double value) {
        return String.format("%.0f%%", value * 100);
    }
}
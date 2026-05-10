package com.viquelle.mikpik.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.viquelle.mikpik.entity.ModEntities;
import com.viquelle.mikpik.entity.shadowgrabber.ShadowGrabberEntity;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ShadowGrabberDebugCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("shadowgrabber")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("spawn")
                                .executes(ctx -> {
                                    ServerLevel level = ctx.getSource().getLevel();
                                    Vec3 pos = ctx.getSource().getPosition();

                                    ShadowGrabberEntity entity = ModEntities.SHADOW_GRABBER.get().create(level);

                                    if (entity == null) {
                                        ctx.getSource().sendFailure(Component.literal("Failed to create ShadowGrabber"));
                                        return 0;
                                    }

                                    entity.moveTo(pos.x, pos.y, pos.z, 0.0f, 0.0f);
                                    entity.setDebugLength(2.0f);
                                    entity.setDebugDirection(new Vec3(0.0, 0.0, 1.0));

                                    level.addFreshEntity(entity);

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Spawned ShadowGrabber"),
                                            false
                                    );

                                    return 1;
                                })
                        )

                        .then(Commands.literal("dir")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("x", FloatArgumentType.floatArg())
                                                .then(Commands.argument("y", FloatArgumentType.floatArg())
                                                        .then(Commands.argument("z", FloatArgumentType.floatArg())
                                                                .executes(ctx -> {
                                                                    ShadowGrabberEntity entity =
                                                                            (ShadowGrabberEntity) EntityArgument.getEntity(ctx, "target");

                                                                    float x = FloatArgumentType.getFloat(ctx, "x");
                                                                    float y = FloatArgumentType.getFloat(ctx, "y");
                                                                    float z = FloatArgumentType.getFloat(ctx, "z");

                                                                    entity.setDebugDirection(new Vec3(x, y, z));

                                                                    ctx.getSource().sendSuccess(
                                                                            () -> Component.literal("Direction set to " + x + ", " + y + ", " + z),
                                                                            false
                                                                    );

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )

                        .then(Commands.literal("length")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 60.0f))
                                                .executes(ctx -> {
                                                    ShadowGrabberEntity entity =
                                                            (ShadowGrabberEntity) EntityArgument.getEntity(ctx, "target");

                                                    float value = FloatArgumentType.getFloat(ctx, "value");

                                                    entity.setDebugLength(value);

                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal("Length set to " + value),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("kill")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(ctx -> {
                                            ShadowGrabberEntity entity =
                                                    (ShadowGrabberEntity) EntityArgument.getEntity(ctx, "target");

                                            entity.discard();

                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("ShadowGrabber removed"),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )
        );
    }
}
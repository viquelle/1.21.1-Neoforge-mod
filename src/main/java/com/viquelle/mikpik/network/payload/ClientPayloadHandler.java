package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.ghost.GhostRespawnClient;
import com.viquelle.mikpik.item.heart.HeartItem;
import com.viquelle.mikpik.sanity.ClientSanityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.viquelle.mikpik.item.heart.HeartItem.raycast;

public class ClientPayloadHandler {
    public static void handleDataOnNetwork(final SanitySyncPayload data, final IPayloadContext context) {
        ClientSanityData.set(data.sanity());
    }

    public static void handleHeartReviveRequest(HeartReviveRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (HeartItem.tryRevive(player)) {
                MikpikMod.LOGGER.info("{} revived, send packet to client", player.getName());
                PacketDistributor.sendToPlayer(player, ReviveSuccessPayload.INSTANCE);
            }
        });
    }

    public static void handleGhostRespawnRequest(GhostRespawnRequest ghostRespawnRequest, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();

            if (!GhostManager.isGhost(player)) return;

            DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(
                    false,
                    DimensionTransition.DO_NOTHING
            );
            Vec3 pos = transition.pos();
            GhostManager.revive(player);

            player.teleportTo(
                    transition.newLevel(),
                    pos.x,
                    pos.y,
                    pos.z,
                    player.getYRot(),
                    player.getXRot()
            );
        });
    }

    public static void handleReviveResult(ReviveSuccessPayload payload, IPayloadContext context) {
        MikpikMod.LOGGER.info("revive result gotcha");
    }

    public static void handePushRequest(PushItemPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (!GhostManager.isGhost(player)) return;;

                HitResult hit = raycast(player, 4.0);

                if (hit instanceof EntityHitResult result) {
                    if (result.getEntity() instanceof ItemEntity item) {
                        Vec3 direction = player.getLookAngle().normalize();
                        double pushStrength = 0.8;

                        item.addDeltaMovement(
                                direction.scale(pushStrength)
                        );
                        item.hurtMarked = true;
                    }
                }
            }
        });
    }

    public static void handleGhostState(GhostStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player == null) return;

            if (payload.isGhost()) {
                GhostManager.becomeGhost(player);
            } else {
                GhostManager.revive(player);
            }
        });
    }
}

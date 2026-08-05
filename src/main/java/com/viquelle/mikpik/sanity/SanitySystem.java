package com.viquelle.mikpik.sanity;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ModAttachments;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.item.items.PlushyItem;
import com.viquelle.mikpik.network.payload.SanitySyncPayload;
import com.viquelle.mikpik.sanity.factor.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class SanitySystem {
    private static final List<SanityFactor> FACTORS = List.of(
            new LightFactor(),
            new ShelterFactor(),
            new NearbyPlayersAndGhostFactor(),
            new FlowerCrownFactor()
    );

    public static float get(Player player) {
        return player.getData(ModAttachments.SANITY);
    }

    public static void set(Player player, float value) {
        player.setData(ModAttachments.SANITY, clamp(value));
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SanitySyncPayload(SanitySystem.get(serverPlayer)));
        }
    }

    public static void add(Player player, float value) {
        set(player, get(player) + value);
    }

    public static void drain(Player player, float value) {
        set(player, get(player) - value);
    }

    private static float clamp(float value) {
        return Math.max(
                SanityConstants.MIN_SANITY,
                Math.min(SanityConstants.MAX_SANITY, value)
        );
    }

    public static float calculateDelta(Player player) {
        float delta = 0.0f;

        for (SanityFactor factor : FACTORS) {
            delta += factor.getDelta(player);
        }

        return delta;
    }

    @SubscribeEvent
    public static void onLoadPlayer(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SanitySyncPayload(SanitySystem.get(serverPlayer)));
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        if (GhostManager.isGhost(player)) {
            set(player, SanityConstants.MAX_SANITY / 2.0f);
        } else {
            float delta = calculateDelta(player);
            add(player, delta);
        }
        validateActivePlushy(player);

//        if (player.tickCount % 4 == 0) {
//            int sky = player.level().getBrightness(LightLayer.SKY, player.blockPosition());
//            int block = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
//
//            player.displayClientMessage(
//                    Component.literal(
//                            "Sanity: " + String.format("%.1f", newValue) +
//                                    " | Δ: " + String.format("%.2f", delta * 20f) +
//                                    " | Sky: " + sky +
//                                    " | Block: " + block +
//                                    " | RawLight " + player.level().getMaxLocalRawBrightness(player.blockPosition()) +
//                                    " | AnyLightAround " + ClientLightManager.sampleLight(player.getPosition(0))
//                    ),
//                    true
//            );
//        }
    }

    private static void validateActivePlushy(Player player) {
        String activePibbleId = player.getData(ModAttachments.ACTIVE_PLUSHY_ID);

        if (activePibbleId.isEmpty()) {
            return;
        }

        if (!hasPlushyWithId(player, activePibbleId)) {
            player.setData(ModAttachments.ACTIVE_PLUSHY_ID, "");
        }
    }

    private static boolean hasPlushyWithId(Player player, String id) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!(stack.getItem() instanceof PlushyItem)) {
                continue;
            }

            if (PlushyItem.getID(stack).equals(id)) {
                return true;
            }
        }

        return false;
    }
}
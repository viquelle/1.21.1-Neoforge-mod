package com.viquelle.mikpik.ghost;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.MikpikModClient;
import com.viquelle.mikpik.item.ModItems;
import com.viquelle.mikpik.network.payload.GhostStatePayload;
import com.viquelle.mikpik.network.payload.PushItemPayload;
import com.viquelle.mikpik.sanity.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static com.viquelle.mikpik.item.heart.HeartItem.pickup;


@EventBusSubscriber(modid = MikpikMod.MODID)
public class GhostManager {
    public static boolean isGhost(Player player) {
        return player.getData(ModAttachments.IS_GHOST);
    }

    public static void revive(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new GhostStatePayload(false));
        }
        player.setData(ModAttachments.IS_GHOST, false);
        updateAbilities(player, false);
    }

    public static void becomeGhost(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new GhostStatePayload(true));
        }
        player.setData(ModAttachments.IS_GHOST, true);
        updateAbilities(player, true);
    }

    public static void updateAbilities(Player player, boolean isGhost) {
        player.getAbilities().invulnerable = isGhost;
        player.getAbilities().flying = isGhost;
        player.getAbilities().mayfly = isGhost;
        player.getAbilities().mayBuild = !isGhost;
        player.onUpdateAbilities();
        player.minorHorizontalCollision = !isGhost;
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        MikpikMod.LOGGER.info("onDeath pushed! {} is dead", player.getName());

        if (GhostManager.isGhost(player)) return;
        event.setCanceled(true);
        player.setHealth(10f);
        player.getInventory().dropAll();
        GhostManager.becomeGhost(player);
        player.dismountTo(player.getX(),player.getY(),player.getZ());
        player.setArrowCount(0);
        player.removeAllEffects();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (GhostManager.isGhost(player)) {
            GhostManager.updateAbilities(player, true);

            PacketDistributor.sendToPlayer(
                    player,
                    new GhostStatePayload(true)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!GhostManager.isGhost(player)) return;

        player.setSprinting(false);
        player.removeAllEffects();
        Vec3 delta = player.getDeltaMovement();
        player.setDeltaMovement(
                delta.x * 0.7,
                delta.y * 0.5,
                delta.z * 0.7
        );
        player.walkAnimation.setSpeed(player.walkAnimation.speed() * 0.5f);
    }

    @SubscribeEvent
    public static void OnVanillaRevive(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            revive(player);
        }
    }
//    @SubscribeEvent
//    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
//        if (isGhost(event.getEntity())) {
//            event.setCanceled(true);
//        }
//    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (isGhost(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

//    @SubscribeEvent
//    public static void onRightClickBlock(PlayerInteractEvent.RightClickItem event) {
//        if (isGhost(event.getEntity())) {
//            event.setCanceled(true);
//        }
//    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        if (isGhost(event.getPlayer())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && isGhost(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !isGhost(player)) return;
        PacketDistributor.sendToServer(PushItemPayload.INSTANCE);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !isGhost(player)) return;
        PacketDistributor.sendToServer(PushItemPayload.INSTANCE);
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        Player player = Minecraft.getInstance().player;

        if (player != null && GhostManager.isGhost(player)) {
            ResourceLocation name = event.getName();
            if (name.equals(VanillaGuiLayers.HOTBAR) ||
                    name.equals(VanillaGuiLayers.PLAYER_HEALTH) ||
                    name.equals(VanillaGuiLayers.ARMOR_LEVEL) ||
                    name.equals(VanillaGuiLayers.FOOD_LEVEL) ||
                    name.equals(VanillaGuiLayers.EXPERIENCE_BAR) ||
                    name.equals(VanillaGuiLayers.CROSSHAIR) ||
                    name.equals(VanillaGuiLayers.AIR_LEVEL)
            ) {
                event.setCanceled(true);
            }
        }
    }

//    @SubscribeEvent
//    public static void onDamage(LivingIncomingDamageEvent event) {
//        if (event.getEntity() instanceof Player player && isGhost(player)) {
//            event.setCanceled(true);
//        }
//    }

}

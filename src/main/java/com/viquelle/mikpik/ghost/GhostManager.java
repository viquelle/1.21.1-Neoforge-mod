package com.viquelle.mikpik.ghost;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.sanity.ModAttachments;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


@EventBusSubscriber(modid = MikpikMod.MODID)
public class GhostManager {
    public static boolean isGhost(Player player) {
        return player.getData(ModAttachments.IS_GHOST);
    }

    public static void revive(Player player) {
        player.setData(ModAttachments.IS_GHOST, false);
        updateAbilities(player, false);
    }

    public static void becomeGhost(Player player) {
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
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MikpikMod.LOGGER.info("onDeath pushed! {} is dead", player.getTabListDisplayName());

        if (GhostManager.isGhost(player)) return;
        event.setCanceled(true);
        player.setHealth(10f);
        player.getInventory().dropAll();
        GhostManager.becomeGhost(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        //MikpikMod.LOGGER.info("onPlayerTick ticked! {} ticking", player.getDisplayName());

        if (!GhostManager.isGhost(player))
            return;

        player.getAbilities().setFlyingSpeed(0.02f);
        updateAbilities(player, true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

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

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickItem event) {
        if (isGhost(event.getEntity())) {
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

//    @SubscribeEvent
//    public static void onDamage(LivingIncomingDamageEvent event) {
//        if (event.getEntity() instanceof Player player && isGhost(player)) {
//            event.setCanceled(true);
//        }
//    }

}

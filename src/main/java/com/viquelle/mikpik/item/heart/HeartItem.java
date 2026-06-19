package com.viquelle.mikpik.item.heart;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.item.ModItems;
import com.viquelle.mikpik.network.payload.ReviveRequestPayload;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class HeartItem extends Item {
    public HeartItem(Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Post event) {

        if (!(event.getEntity() instanceof ItemEntity item))
            return;

        if (!item.getItem().is(ModItems.HEART.get()))
            return;

        item.setNoGravity(true);

        Vec3 motion = item.getDeltaMovement();

        item.setDeltaMovement(
                motion.x * 0.9,
                motion.y * 0.9,
                motion.z * 0.9
        );
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (tryRevive(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (tryRevive(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide || !GhostManager.isGhost(player)) return;

        HitResult hit = raycast(player, 4.0);

        if (hit instanceof EntityHitResult result) {
            if (result.getEntity() instanceof ItemEntity entity) {
                if (entity.getItem().is(ModItems.HEART.get())) {
                    MikpikMod.LOGGER.debug("Client: sending revive request");
                    PacketDistributor.sendToServer(ReviveRequestPayload.INSTANCE);
                }
            }
        }
    }

    public static HitResult raycast(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0f);
        Vec3 end = start.add(direction.scale(range));

        AABB searchArea = player.getBoundingBox()
                .expandTowards(direction.scale(range))
                .inflate(1.0);

        HitResult result = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                searchArea,
                entity -> entity instanceof ItemEntity,
                range * range
        );
        MikpikMod.LOGGER.info("{}", result);
        return result;
    }

    public static boolean tryRevive(Player player) {
        if (player.level().isClientSide) return false;

        if (!GhostManager.isGhost(player)) return false;

        HitResult hit = raycast(player, 4.0);

        if (hit instanceof EntityHitResult entityHitResult) {
            if (entityHitResult.getEntity() instanceof ItemEntity itemEntity) {
                if (itemEntity.getItem().is(ModItems.HEART.get())) {
                    GhostManager.revive(player);
                    itemEntity.discard();

                    return true;
                }
            }
        }
        return false;
    }
//    @SubscribeEvent
//    public static void onLeftClick(AttackEntityEvent event) {
//        if (event.getTarget().level().isClientSide) return;
//        if (!(event.getTarget() instanceof ItemEntity item)) return;
//
//        Vec3 push = item.position().subtract(event.getEntity().position()).normalize();
//        item.setDeltaMovement(item.getDeltaMovement().add(push.scale(0.5)));
//        item.hurtMarked = true; // Синхронизирует движение с клиентом
//
//        event.setCanceled(true); // Отменяет урон и предотвращает ванильный кик
//    }

    public static void pickup(Player player, ItemEntity item) {
        if (!player.level().isClientSide) {
            MikpikMod.LOGGER.info("adding item {}", item.getName());
            if (player.addItem(item.getItem())) {
                item.discard();
            }
        }
    }
}

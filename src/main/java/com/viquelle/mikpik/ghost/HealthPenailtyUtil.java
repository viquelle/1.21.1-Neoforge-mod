package com.viquelle.mikpik.ghost;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.sanity.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class HealthPenailtyUtil {
    public static final ResourceLocation PENALTY_ID =
            ResourceLocation.fromNamespaceAndPath(
                    MikpikMod.MODID,
                    "penalty"
            );

    public static void addPenalty(Player player, double amount) {
        double current = player.getData(ModAttachments.PENALTY);
        MikpikMod.LOGGER.info("current: {}", current);
        double newPenalty = Math.min(0.75, current + (1 - current) * amount);
        MikpikMod.LOGGER.info("new: {}", newPenalty);
        applyPenalty(player, newPenalty);
    }

    private static void applyPenalty(Player player, double penalty) {
        MikpikMod.LOGGER.info("applyPenalty!");
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        MikpikMod.LOGGER.info("attribute not null");
        player.setData(ModAttachments.PENALTY, (float) penalty);

        attribute.addOrReplacePermanentModifier(
                new AttributeModifier(
                        PENALTY_ID,
                        -penalty,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
        );

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player old = event.getOriginal();
        Player fresh = event.getEntity();

        AttributeInstance oldAttr = old.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance newAttr = fresh.getAttribute(Attributes.MAX_HEALTH);

        if (oldAttr == null || newAttr == null) return;

        AttributeModifier modifier = oldAttr.getModifier(PENALTY_ID);
        if (modifier != null) {
            newAttr.addPermanentModifier(modifier);
        }
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        double penalty = player.getData(ModAttachments.PENALTY);

        if (penalty > 0) {
            applyPenalty(
                    player,
                    penalty
            );
        }
    }
}

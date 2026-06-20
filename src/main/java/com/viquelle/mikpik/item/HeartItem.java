package com.viquelle.mikpik.item;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

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
                motion.x * 0.85,
                motion.y * 0.85,
                motion.z * 0.85
        );
    }
}

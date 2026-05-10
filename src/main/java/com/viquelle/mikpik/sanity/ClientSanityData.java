package com.viquelle.mikpik.sanity;

import com.viquelle.mikpik.MikpikMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class ClientSanityData {
    private static float sanity = 100.0f;

    public static float get() {
        return sanity;
    }

    public static void set(float value) {
        sanity = Math.max(0f, Math.min(100f, value));
    }

    @SubscribeEvent
    public static void ontick(PlayerTickEvent.Post event) {
//        MikpikMod.LOGGER.info("{}", get());
    }
}

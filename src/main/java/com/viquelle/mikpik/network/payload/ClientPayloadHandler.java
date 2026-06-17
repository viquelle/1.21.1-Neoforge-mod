package com.viquelle.mikpik.network.payload;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.sanity.ClientSanityData;
import com.viquelle.mikpik.sanity.ModAttachments;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleDataOnNetwork(final SanitySyncPayload data, final IPayloadContext context) {
        ClientSanityData.set(data.sanity());
    }
}

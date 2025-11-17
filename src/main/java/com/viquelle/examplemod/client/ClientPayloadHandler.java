package com.viquelle.examplemod.client;

import com.viquelle.examplemod.client.sanity.SanityData;
import com.viquelle.examplemod.network.SanityPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleSanityData(final SanityPayload payload, final IPayloadContext context) {
        Minecraft.getInstance().execute(() -> {
            SanityData.update(payload.sanity());
        });
    }
}

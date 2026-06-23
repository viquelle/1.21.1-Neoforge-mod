package com.viquelle.mikpik.network;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.network.payload.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SanitySyncPayload.TYPE, SanitySyncPayload.STREAM_CODEC, ClientPayloadHandler::handleDataOnNetwork);
        registrar.playToServer(HeartReviveRequestPayload.TYPE, HeartReviveRequestPayload.STREAM_CODEC, ClientPayloadHandler::handleHeartReviveRequest);
        registrar.playToServer(PushItemPayload.TYPE, PushItemPayload.STREAM_CODEC, ClientPayloadHandler::handePushRequest);
        registrar.playToClient(ReviveSuccessPayload.TYPE, ReviveSuccessPayload.STREAM_CODEC, ClientPayloadHandler::handleReviveResult);
        registrar.playToClient(GhostStatePayload.TYPE, GhostStatePayload.STREAM_CODEC, ClientPayloadHandler::handleGhostState);
        registrar.playToServer(GhostRespawnRequest.TYPE, GhostRespawnRequest.STREAM_CODEC, ClientPayloadHandler::handleGhostRespawnRequest);
    }
}

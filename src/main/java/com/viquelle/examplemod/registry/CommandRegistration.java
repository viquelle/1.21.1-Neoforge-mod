package com.viquelle.examplemod.registry;

import com.viquelle.examplemod.sanity.SanityCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandRegistration {
    public static void register() {
        NeoForge.EVENT_BUS.register(new CommandRegistration());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SanityCommands.register(event.getDispatcher());
    }
}

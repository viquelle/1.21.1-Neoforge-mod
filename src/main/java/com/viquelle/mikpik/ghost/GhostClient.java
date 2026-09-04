package com.viquelle.mikpik.ghost;

import com.mojang.blaze3d.platform.InputConstants;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.blockentity.MeatEffigyBlockEntity;
import com.viquelle.mikpik.network.payload.GhostRespawnRequest;
import com.viquelle.mikpik.world.Gameplay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class GhostClient {
    private static int holdTicks = 0;
    private static final int maxHoldTicks = 100;
    private static boolean shouldTryRevive = false;
    private static boolean hasEffigy = false;

    public static final KeyMapping RESPAWN_KEY = new KeyMapping(
            "key." + MikpikMod.MODID + ".ghost_resurrect", InputConstants.KEY_R, "key.categories." + MikpikMod.MODID
    );

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(RESPAWN_KEY);
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
                    name.equals(VanillaGuiLayers.EXPERIENCE_LEVEL) ||
                    name.equals(VanillaGuiLayers.CROSSHAIR) ||
                    name.equals(VanillaGuiLayers.AIR_LEVEL)
            ) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean checkShoulding(Player player) {
        if (hasEffigy) {
            return true;
        } else return !Gameplay.isDst(player.level());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return;
        if (player.tickCount % 5 == 0) {
            hasEffigy = MeatEffigyBlockEntity.hasBinding(player);
            shouldTryRevive = checkShoulding(player);
        }

        if (!GhostManager.isGhost(player) || !shouldTryRevive) {
            holdTicks = 0;
            return;
        }

        if (RESPAWN_KEY.isDown()) {
            holdTicks++;
        } else {
            holdTicks = Math.max(holdTicks - 1, 0);
        }

        if (holdTicks >= maxHoldTicks) {
            PacketDistributor.sendToServer(GhostRespawnRequest.INSTANCE);
            holdTicks = 0;
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        LocalPlayer player = mc.player;

        if (player == null || !GhostManager.isGhost(player)) return;

        Component text;
        Component keyName = RESPAWN_KEY.getTranslatedKeyMessage();

        if (shouldTryRevive) {
            text = hasEffigy ?
                    Component.translatable("gui." + MikpikMod.MODID + ".ghost_resurrect_effigy", keyName):
                    Component.translatable("gui." + MikpikMod.MODID + ".ghost_resurrect", keyName);
        } else return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        float progress = holdTicks / 100f;

        event.getGuiGraphics().drawCenteredString(
                mc.font,
                text,
                width/2,
                height/2 - 20 + 100,
                0xFFFFFF
        );

        int size = 150;

        event.getGuiGraphics().fill(
                width/2 - size/2,
                height/2 + 100,
                width/2 + size/2,
                height/2 + 8 + 100,
                0xFF555555
        );

        event.getGuiGraphics().fill(
                width / 2 - size / 2,
                height / 2 + 100,
                width / 2 - size / 2 + (int)(size * progress),
                height / 2 + 8 + 100,
                0xFFFFFFFF
        );
    }

}

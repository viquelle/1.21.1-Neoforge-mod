package com.viquelle.mikpik.ghost;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.network.payload.GhostRespawnRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class GhostClient {
    private static int holdTicks = 0;
    private static int maxHoldTicks = 100;

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
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || !GhostManager.isGhost(player)) {
            holdTicks = 0;
            return;
        }

        if (mc.options.keyAttack.isDown()) {
            holdTicks++;
        } else {
            holdTicks = Math.max(holdTicks-1,0);
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

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        float progress = holdTicks / 100f;

        event.getGuiGraphics().drawCenteredString(
                mc.font,
                "Hold LMB to respawn",
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

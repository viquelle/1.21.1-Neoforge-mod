package com.viquelle.mikpik;

import com.viquelle.mikpik.coloredlights.ActiveLight;
import com.viquelle.mikpik.coloredlights.ColoredLightBuffer;
import com.viquelle.mikpik.coloredlights.ColoredLightScanner;
import com.viquelle.mikpik.coloredlights.ColoredLightsUploader;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowForearmModel;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowHandModel;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowPortalModel;
import com.viquelle.mikpik.light.ClientLightManager;
import com.viquelle.mikpik.light.source.*;
import foundry.veil.api.client.render.VeilRenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.List;

@Mod(value = MikpikMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class MikpikModClient {
    public MikpikModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        MikpikMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        MikpikMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        ClientLightManager.register(new NetherStarLightSource());
        ClientLightManager.register(new LanternLightSource());
        ClientLightManager.register(new TorchLightSource());
        ClientLightManager.register(new PlayerAmbientLightSource());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ClientLightManager.clear();
        }
    }

    private static boolean enabled = false;
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        ColoredLightsUploader.init();
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            ClientLightManager.tick(mc.level, mc.player, event.getPartialTick().getGameTimeDeltaPartialTick(true));
            SanityPostShaderHandler.init();
            if (!enabled) {
                enabled = true;
                var renderer = VeilRenderSystem.renderer();
                if (renderer != null) {
                    SanityPostShaderHandler.tick();
                }
            }
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            ColoredLightScanner.buildVisibleLightBuffer(mc.player);
            ColoredLightsUploader.updateUniforms(event.getCamera().getPosition());
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                ShadowForearmModel.LAYER_LOCATION,
                ShadowForearmModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ShadowHandModel.LAYER_LOCATION,
                ShadowHandModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ShadowPortalModel.LAYER_LOCATION,
                ShadowPortalModel::createBodyLayer
        );
    }
}

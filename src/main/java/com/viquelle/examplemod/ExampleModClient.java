package com.viquelle.examplemod;

import com.viquelle.examplemod.client.ClientPayloadHandler;
import com.viquelle.examplemod.client.FlashlightRenderer;
import com.viquelle.examplemod.client.HudRenderer;
import com.viquelle.examplemod.network.SanityPayload;
import foundry.veil.api.client.registry.LightTypeRegistry;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.util.FastNoiseLite;
import foundry.veil.impl.client.render.light.AreaLightRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Vector;

@Mod(value = ExampleMod.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ExampleModClient {
    static boolean lightInit = false;
    static AreaLightData light1;
    public ExampleModClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.register(HudRenderer.class);
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent e) {
        e.registrar("1").playToClient(SanityPayload.TYPE, SanityPayload.STREAM_CODEC, ClientPayloadHandler::handleSanityData);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ExampleMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        ExampleMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (!lightInit && Minecraft.getInstance().level != null) {
            lightInit = true;
            light1 = createLight();
            VeilRenderSystem.renderer().getLightRenderer().addLight(light1);

        }
//        for (LightRenderHandle<AreaLightData> data : VeilRenderSystem.renderer().getLightRenderer().getLights(LightTypeRegistry.AREA.get())) {
//            AreaLightData b = data.getLightData();
//            ExampleMod.LOGGER.info("{}",b.getType());
//            if (Minecraft.getInstance().level.getTimeOfDay(0) == 0.5f) {
//                data.free();
//            }
//        }
        if (lightInit && Minecraft.getInstance().level != null) {
            LocalPlayer p = Minecraft.getInstance().player;
            Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
            Quaternionf camRot = new Quaternionf(cam.rotation()).rotateY(-(float)Math.PI).invert();
            light1.getPosition().set(p.getEyePosition().x,p.getEyePosition().y,p.getEyePosition().z);
            ExampleMod.LOGGER.info("{} {}",cam.rotation(),light1.getOrientation());
            light1.getOrientation().set(camRot);
        }
    }

    private static AreaLightData createLight() {
        AreaLightData light = new AreaLightData();
        light.getPosition().set(0f,0f,0f);
        light.setBrightness(0.7f).setDistance(32.0f).setAngle(0.6f).setSize(0.1f,0.1f);
        light.getOrientation().rotateY((float)Math.toRadians(180));
        light.setColor(0xAAAAFF);
        return light;
    }
}

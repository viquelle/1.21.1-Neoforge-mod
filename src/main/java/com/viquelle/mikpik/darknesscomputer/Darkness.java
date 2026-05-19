package com.viquelle.mikpik.darknesscomputer;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class Darkness {
    static Logger log = LogManager.getLogger("Darkness");
    private static float[][] LUMINANCE = new float[16][16];
    public static boolean enabled = true;

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!Darkness.enabled) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.getEyeInFluidType() == (net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value())) {
            event.setRed(0.01f);
            event.setGreen(0.01f);
            event.setBlue(0.02f);
        }
    }

    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        if (!Darkness.enabled) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        if (player.getEyeInFluidType() == (net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value())) {
            event.setNearPlaneDistance(2.0f);
            event.setFarPlaneDistance(10.0f);
            event.setCanceled(true);
        }
    }

    public static float skyFactor(Level world, float partialTick) {
        if (world == null || !world.dimensionType().hasSkyLight()) return 0.0f;

        float time = world.getTimeOfDay(partialTick);
        float moon = world.getMoonBrightness();
        float transition = 0.015f;
        if (time >= 0.25f - transition && time < 0.25f) {
            float t = (time - (0.25f - transition)) / transition;
            return Mth.lerp(t, 1.0f, moon);
        } else if (time >= 0.25f && time <= 0.75f - transition) {
            return moon;
        } else if (time > 0.75f - transition && time <= 0.75f) {
            float t = (time - (0.75f - transition)) / transition;
            return Mth.lerp(t, moon, 1.0f);
        } else {
            return 1.0f; // Иначе это день
        }
    }

    public static void updateLuminance(Minecraft client, GameRenderer worldRenderer, float tickDelta, float prevFlicker) {
        final ClientLevel world = client.level;

        if (world == null || client.player == null) return;
        if (client.player.hasEffect(MobEffects.NIGHT_VISION) || client.player.hasEffect(MobEffects.CONDUIT_POWER) || world.getSkyFlashTime() > 0) {
            enabled = false;
            return;
        }
        enabled = true;

        float skyIntensity = skyFactor(world, tickDelta);
        DimensionType dim = world.dimensionType();
        for (int b = 0; b < 16; b++) {
            float block = LightTexture.getBrightness(dim,b);
            block = block * (prevFlicker * 0.1f + 1.0f);
            for (int s = 0; s < 16; s++ ) {
                float light;
                if (b == 0 && s == 0) {
                    light = 0.0f;
                } else {
                    float skyAccess = s / 15.0f;
                    skyAccess *= Mth.lerp(skyIntensity, 0.02f,0.4f);
                    light = block + skyAccess * (1.0f - block);
                }
                light = light; //* light * (2.0f - light);
                LUMINANCE[b][s] = Mth.clamp(light, 0.0f, 1f);
            }
        }
    }

    /**
     * Получает пиксель из текущего LightMap и пересчитывает его яркость.
     */
    public static int darken(int color, int blockIndex, int skyIndex) {
        final float lTarget = LUMINANCE[blockIndex][skyIndex];
        final float r = (color & 0xFF) / 255f;
        final float g = ((color >> 8) & 0xFF) / 255f;
        final float b = ((color >> 16) & 0xFF) / 255f;
        final float l = luminance(r,g,b);
        final float f = l > 0 ? Math.min(1, lTarget/ l) : 0;

        return f == 1f ? color : 0xFF000000 | Math.round(f * r * 255) | Math.round(f * g * 255) << 8 | Math.round(f * b * 255) << 16;
    }

    /**
     *
     * @return насколько свет кажется ярким для человеческого глаза [0;1]
     */
    public static float luminance(float r, float g, float b) {
        return r * 0.2126f + g * 0.7152f + b * 0.0722f;
    }


}
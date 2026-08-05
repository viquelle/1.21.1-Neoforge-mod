package com.viquelle.mikpik.light;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.client.darknesscomputer.Darkness;
import com.viquelle.mikpik.light.source.LightSource;
import com.viquelle.mikpik.light.source.UpdatePhase;
import com.viquelle.mikpik.network.payload.DynamicBrightPayload;
import com.viquelle.mikpik.sanity.SanityConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class ClientLightManager {
    private static final List<LightSource> SOURCES = new ArrayList<>();
    private static float lastFrameTick = 0;
    public static void register(LightSource source) {
        SOURCES.add(source);
    }

    public static void tick(Level level, Player player, float partialTick) {
        for (LightSource source : SOURCES)
            if (source.getUpdatePhase() == UpdatePhase.NORMAL)
                source.tick(level, partialTick);

        for (LightSource source : SOURCES)
            if (source.getUpdatePhase() == UpdatePhase.AFTER_LIGHTS)
                source.tick(level, partialTick);
        lastFrameTick = level.getGameTime() + partialTick;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || !level.isClientSide || player == null) return;
        PacketDistributor.sendToServer(
                new DynamicBrightPayload(sampleLight(player.getEyePosition(1f)))
        );
    }

    public static void clear() {
        SOURCES.forEach(LightSource::destroy);
        lastFrameTick = 0;
    }

    public static float sampleLight(Vec3 pos) {
        float result = 0f;
        for (LightSource source : SOURCES) {
            for (LightHandle<?> handle : source.getLights()) {

                if (!handle.affectDarkness) continue;

                Vec3 lightPos = handle.getPosition();
                float brightness = handle.getBrightness();

                double dx = pos.x - lightPos.x;
                double dy = pos.y - lightPos.y;
                double dz = pos.z - lightPos.z;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < 0.01) {
                    result = Math.max(brightness, result);
                    continue;
                }

                if (handle instanceof PointLightHandle point) {
                    double radius = point.getRadius();
                    if (distSq > radius * radius) continue;

                    float dist = (float) Math.sqrt(distSq);
                    float t = 1.0f - (dist / (float) radius);

                    float influence = brightness * (float) Math.pow(t, 2.0f);
                    result = Math.max(influence, result);
                    continue;
                }

                if (handle instanceof SpotLightHandle area) {
                    double range = area.getRange();
                    if (distSq > range * range) continue;

                    Vec3 dir = area.getForward();
                    float invDist = (float) Mth.fastInvSqrt(distSq);
                    float dist = (float) (distSq * invDist);
                    float dot = (float) (
                            dir.x * dx * invDist +
                                    dir.y * dy * invDist +
                                    dir.z * dz * invDist
                    );

                    float halfAngleCos = (float) Math.cos(area.getAngle() * 0.5f);

                    if (dot < halfAngleCos) continue;

                    float angleFactor = (dot - halfAngleCos) / (1.0f - halfAngleCos);
                    angleFactor = Math.max(0f, Math.min(1f, angleFactor));

                    float distanceFactor = 1.0f - (dist / (float) range);

                    float influence = brightness * (float) Math.pow(distanceFactor, 2.0f) * angleFactor;
                    result = Math.max(result, influence);
                }
            }
        }

        return result;
    }

    public static boolean isDarkOnPos(Vec3 pos, Level level, float partialTick) {
        float veilLight = sampleLight(pos) * SanityConstants.VEIL_NORMALIZATION;
        float blockLight = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(pos));
        float skyLight = Darkness.posSkyLight(pos, level, partialTick);
        float localBrightness = Math.max(Math.max(blockLight, skyLight), veilLight);
        return localBrightness <= SanityConstants.BRIGHTNESS_THRESHOLD;
    }

    public static float getLastFrameTick() {
        return lastFrameTick;
    }
}
package com.viquelle.mikpik.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AreaLightHandle extends LightHandle<AreaLightData> {
    private final Quaternionf orientation = new Quaternionf();
    private float angle;
    private float distance;

    public AreaLightHandle(float angle, float distance, float brightness, int color, boolean occlusion) {
        super(new AreaLightData());
        this.angle = angle;
        this.distance = distance;
        data.setAngle(angle)
                .setDistance(distance)
                .setBrightness(brightness)
                .setColor(color)
                .setSize(0.01f, 0.01f)   // размер источника (можно настроить)
                .setOcclusionEnabled(occlusion);
    }

    /**
     * Устанавливает ориентацию света по направлению взгляда игрока.
     */
    public void setOrientationFromPlayer(Player player, float partialTick) {
        float yaw = (float) Math.toRadians(player.getYRot());
        float pitch = (float) Math.toRadians(player.getXRot());
        orientation.identity().rotationXYZ(-pitch, yaw, 0f);
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.getOrientation().set(orientation));
        }
    }

    public void setAngle(float angle) {
        this.angle = angle;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setAngle(angle));
        }
    }

    public void setDistance(float distance) {
        this.distance = distance;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setDistance(distance));
        }
    }

    @Override
    protected void updatePositionInData(Vec3 pos) {
        data.getPosition().set((float) pos.x, (float) pos.y, (float) pos.z);
    }
}
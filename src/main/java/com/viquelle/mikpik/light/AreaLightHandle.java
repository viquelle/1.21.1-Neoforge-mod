package com.viquelle.mikpik.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AreaLightHandle extends LightHandle<AreaLightData> {
    private final Quaternionf direction = new Quaternionf();
    private float angle;
    private float range;
    private boolean occlusion;

    public AreaLightHandle(float angle, float range, float brightness, int color, boolean occlusion) {
        super(new AreaLightData(), brightness, color, true);
        this.angle = angle;
        this.range = range;
        this.brightness = brightness;
        this.color = color;
        this.occlusion = occlusion;
        data.setAngle(angle)
                .setDistance(range)
                .setBrightness(brightness)
                .setColor(color)
                .setSize(0.01f, 0.01f)   // размер источника (можно настроить)
                .setOcclusionEnabled(occlusion);
    }

    public AreaLightHandle(float angle, float range, float brightness, int color, boolean occlusion, boolean countsAsLight) {
        super(new AreaLightData(), brightness, color, countsAsLight);
        this.angle = angle;
        this.range = range;
        this.brightness = brightness;
        this.color = color;
        this.occlusion = occlusion;
        this.countsAsLight = countsAsLight;
        data.setAngle(angle)
                .setDistance(range)
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
        direction.identity().rotationXYZ(-pitch, yaw, 0f);
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.getOrientation().set(direction));
        }
    }

    public void setAngle(float angle) {
        this.angle = angle;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setAngle(angle));
        }
    }
    public float getAngle() {
        return angle;
    }

    public void setRange(float range) {
        this.range = range;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setDistance(range));
        }
    }
    public float getRange() {
        return range;
    }

    public Quaternionf getDirection() {
        return direction;
    }

    @Override
    protected void updatePositionInData(Vec3 pos) {
        data.getPosition().set((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public Vec3 getForward() {
        Vector3f v = new Vector3f(0, 0, -1);
        direction.transform(v);
        return new Vec3(v.x, v.y, v.z).normalize();
    }
}